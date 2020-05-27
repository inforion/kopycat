/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Non-free licenses may also be purchased from INFORION, LLC, 
 * for users who do not want their programs protected by the GPL. 
 * Contact us for details kopycat@inforion.ru
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package ru.inforion.lab403.kopycat.interactive

import io.javalin.Javalin
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.annotations.RESTExtension
import ru.inforion.lab403.kopycat.consoles.AConsole
import ru.inforion.lab403.kopycat.gdbstub.GDBServer
import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry
import ru.inforion.lab403.kopycat.settings
import java.io.File
import java.security.MessageDigest
import java.util.*
import java.util.logging.Level
import kotlin.collections.HashMap

class REST(val port: Int, private val console: AConsole, val kopycat: Kopycat) {

    companion object {
        private val log = logger(Level.FINE)
    }

    private val defaultPluginDir = "extras/plugins/"

    data class Response(val status: Int, val message: String?)

    private val app = Javalin.create()

    private fun calcMd5(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("MD5")
        return digest.digest(bytes).hexlify()
    }

    private var reflection: Reflections? = null

    private fun loadOrGetReflections(): Reflections {
        if (reflection == null) {
            val helper = ClasspathHelper.forJavaClassPath()
            val subtypeScanner = SubTypesScanner(false)
            val annotationsScanner = TypeAnnotationsScanner()
            // may be very long in debug, set breakpoint after this line
            reflection = Reflections(helper, subtypeScanner, annotationsScanner)
        }
        return reflection!!
    }

    private fun reloadReflections(): Reflections {
        reflection = null
        return loadOrGetReflections()
    }

    private fun Javalin.initializeKopycatREST() {
        if (!settings.enableRestApi) {
            log.severe { "REST interface wasn't enabled in settings!" }
            return
        }

        log.info { "Initialize Javalin RESTful server..." }

        port(port)
        exception(Exception::class.java) { e, _ -> e.printStackTrace() }
        error(404) { it.json("not found") }

        log.info { "Configuring Javalin RESTful API routes..." }

        routes {
            get("/console") {
                data class ConsoleResponse(val status: Int, val name: String)
                it.json(ConsoleResponse(0, console.name))
            }

            get("/eval") {
                data class EvalResponse(val status: Int)

                val expression = it.queryParam("expression")!!
                val result = console.eval(expression)
                it.json(EvalResponse(result.status))
            }

            get("/execute") {
                data class ExecuteResponse(val status: Int, val retval: String)

                val statement = it.queryParam("statement")!!
                val result = console.execute(statement)
                it.json(ExecuteResponse(result.status, result.message!!))
            }

            get("/pcread") {
                data class PcReadResponse(val status: Int, val message: String?, val pc: String)
                if (kopycat.isTopModulePresented)
                    it.json(PcReadResponse(0, "PC value", kopycat.pcRead().hex8))
                else
                    it.json(Response(-1, "Top module is null. It looks like emulator state is disabled"))
            }

            get("/pcwrite") {
                data class PcWriteResponse(val status: Int, val message: String?, val pc: String)
                val pcValue = it.queryParam("value")!!
                if (kopycat.isTopModulePresented) {
                    kopycat.pcWrite(pcValue.hexAsULong)
                    it.json(PcWriteResponse(0, "PC value", kopycat.pcRead().hex8))
                } else it.json(Response(-1, "Top module is null. It looks like emulator state is disabled"))
            }

            get("/get_state_emulator") {
                data class EmulatorStateResponse(val status: Int, val message: String?, val state: String)
                if(!kopycat.isGdbServerPresented && !kopycat.isTopModulePresented)
                    it.json(EmulatorStateResponse(0,"Emulator state", "disabled"))
                else if(kopycat.isGdbServerPresented && kopycat.isTopModulePresented){
                    val response = if (kopycat.gdbClientProcessing)
                        EmulatorStateResponse(0, "Emulator state", "working")
                    else
                        EmulatorStateResponse(0, "Emulator state", "await")
                    it.json(response)
                }
                else
                    it.json(Response(1, "gdbServer and platform has different states"))
            }

            get("/close") {
                kopycat.close()
                it.json(Response(0, "Kopycat successfully closed..."))
            }

            get("/exit") {
                it.json(Response(0, "Kopycat exiting..."))
                kopycat.exit()
                stop()
            }

            get("/getAvailablePlatforms") {
                data class AvailablePlatformsResponse(val status: Int, val message: String?, val platforms: Map<String, Collection<String>>)
//                val dirs = File(defaultPluginDir).list()
//                        .filter { entry -> File(defaultPluginDir, entry).isDirectory }
//                val pluginLine = dirs.joinToString(separator = ",") { name -> "$name:${File(defaultPluginDir, name)}" }
//                val libs = ModuleLibraryRegistry.create(null, pluginLine)
                val platforms = kopycat.registry?.getAvailableTopModules()?.map { lib -> lib.name to lib.getModulesNames() }?.toMap() ?: HashMap()
                it.json(AvailablePlatformsResponse(0, "Available platforms data", platforms))
            }

            get("/createLibrary") {
                val directoryName = it.queryParam("library_name")!!
                File(defaultPluginDir, directoryName).mkdir()
                it.json(Response(0, "Directory $directoryName created"))
            }

            get("/deleteLibrary") {
                val directoryName = it.queryParam("library_name")!!
                File(defaultPluginDir, directoryName).deleteRecursively()
                it.json(Response(0, "Directory $directoryName deleted"))
            }

            get("/sendPlugin") {
                val directoryName = it.queryParam("library_name")!!
                val pluginName = it.queryParam("plugin_name")!!
                val base64Data = it.queryParam("file_data")!!
                val decodedBytes = Base64.getDecoder().decode(base64Data)
                val libraryPath = java.nio.file.Paths.get(defaultPluginDir, directoryName).toString()
                File(libraryPath, pluginName).writeBytes(decodedBytes)
                it.json(Response(0, "File $pluginName places to $directoryName"))
            }

            get("/deletePlugin") {
                val directoryName = it.queryParam("library_name")!!
                val pluginName = it.queryParam("plugin_name")!!
                val libraryPath = java.nio.file.Paths.get(defaultPluginDir, directoryName).toString()
                File(libraryPath, pluginName).delete()
                it.json(Response(0, "File $pluginName deleted from $directoryName"))
            }

            get("/downloadFile") {
                data class DownloadFileResponse(val status: Int, val data: String, val md5: String)

                val filename: String = it.queryParam("filename")!!
                val path = java.nio.file.Paths.get(kopycat.workingDirectory, filename).toString()
                val bytes = File(path).readBytes()
                val encodedBytes = Base64.getEncoder().encodeToString(bytes)
                val hash = calcMd5(bytes)
                it.json(DownloadFileResponse(0, encodedBytes, hash))
            }

            get("/create") {
                val plugin: String? = it.queryParam("plugin")
                val library: String? = it.queryParam("library")
                val parameters: String? = it.queryParam("parameters")
                val snapshot: String? = it.queryParam("snapshot")
                val gdbPort: Int? = it.queryParam("gdbPort")?.toInt()
                val registryLine: String? = it.queryParam("registryPath")
                val traceable: Boolean = it.queryParam("traceable")?.toBoolean() ?: false

                if (registryLine != null)
                    kopycat.registry = ModuleLibraryRegistry.create(registryLine)

                log.info { "Starting emulator session with params:\n\tplugin: $plugin" +
                        "\n\tlibrary: $library\n\tparameters: $parameters" +
                        "\n\tsnapshot: $snapshot\n\tgdbPort: $gdbPort\n\ttraceable: $traceable" }

                if (plugin == null || library == null) {
                    it.json(Response(-1, "Plugin and library must be specified explicitly!"))
                    return@get
                }

                log.fine { kopycat.registry?.getAvailableAllModules()?.joinToString("\n") }

                val gdb = if (gdbPort != null) {
                    val gdbBinaryProto: Boolean = it.queryParam("gdbBinaryProto")?.toBoolean() ?: false
                    GDBServer(gdbPort, true, gdbBinaryProto)
                } else null

                try {
                    kopycat.open(plugin, library, snapshot, parameters, traceable, gdb)
                } catch (ex: Exception) {
                    gdb?.close()
                    log.severe { ex.toString() }
                    ex.printStackTrace()
                    it.json(Response(-1, ex.toString()))
                    return@get
                }

                it.json(Response(0, "Kopycat successfully started..."))
            }

            get("/signatureFile") {
                data class SignatureFileResponse(val status: Int, val destPath: String, val md5: String)

                val filename: String = it.queryParam("filename")!!
                val path = java.nio.file.Paths.get(kopycat.workingDirectory, filename).toString()
                if (File(path).isFile) {
                    val hash = calcMd5(File(path).readBytes())
                    log.info { "Hash of file $path is $hash" }
                    it.json(SignatureFileResponse(0, path, hash))
                } else {
                    it.json(Response(-1, "File wasn't found $path"))
                }
            }

            post("/uploadFile") {
                data class UploadFileResponse(
                        val status: Int,
                        val destPath: String,
                        val dirWasCreated: Boolean,
                        val fileSize: Int,
                        val md5: String)

                val filename: String = it.formParam("filename")!!
                val base64Data: String = it.formParam("base64Data")!!

                val decodedBytes = Base64.getDecoder().decode(base64Data)
                val pathDir = java.nio.file.Paths.get(kopycat.workingDirectory).toString()
                val pathFile = java.nio.file.Paths.get(pathDir, filename).toString()
                val dirWasCreated = if (!File(pathDir).isDirectory) {
                    File(pathDir).mkdirs()
                    log.info { "Directory $pathDir does not exist. The directory was created successfully" }
                    true
                } else false
                File(pathFile).writeBytes(decodedBytes)
                log.info { "File $filename is written to $pathFile (${decodedBytes.size} bytes)" }
                val hash = calcMd5(decodedBytes)
                it.json(UploadFileResponse(0, pathFile, dirWasCreated, decodedBytes.size, hash))
            }

            get("/takeSnapshot") {
                val snapshotName: String = it.queryParam("snapshot_name")!!
                val result = kopycat.save(snapshotName)
                if(result)
                    it.json(Response(0, "Snapshot was created successfully"))
                else
                    it.json(Response(-1, "Some error in taking snapshot"))
            }

            get("/reset") {
                if(kopycat.isTopModulePresented) {
                    kopycat.reset()
                    it.json(Response(0, "Snapshot was resered successfully"))
                } else {
                    it.json(Response(-1, "Some error in resetting. Device is null"))
                }
            }

            get("/memoryWrite") {
                if(kopycat.isTopModulePresented) {
                    val address = it.queryParam("address")!!.toLong(16)
                    val data = it.queryParam("data")!!.unhexlify()
                    kopycat.dbgStore(address, data)
                    it.json(Response(0, "Data was written to ${address.hex8} (${data.size} bytes)"))
                } else {
                    it.json(Response(-1, "Some error in writing. Device is null"))
                }
            }

            get("/restore") {
                if(kopycat.isSerializerPresented) {
                    kopycat.restore()
                } else {
                    it.json(Response(-1, "Some error in restoring. Device is null"))
                }
            }

            get("/deserialize") {
                try{
                    val snapshot: String? = it.queryParam("snapshot")
                    kopycat.load(snapshot)
                    it.json(Response(0, "Target deserialized"))
                } catch (ex: Exception){
                    it.json(Response(-1, "Some error in deserializing: $ex"))
                }
            }

            get("/start_interpr") { TODO() }

            get("/reload_extensions") {
                try {
                    reloadReflections()
                } catch (ex: Exception) {
                    it.json(Response(-1, "Can't load reflection: ${ex.message}"))
                }
            }

            get("/extension") {
                val objectName = it.queryParam("class")
                val methodName = it.queryParam("method")
                try {
                    val obj = loadOrGetReflections()
                            .getTypesAnnotatedWith(RESTExtension::class.java)
                            .first { cls -> cls.name.substringAfterLast(".") == objectName }
                    val objInstance = obj
                            .constructors
                            .first()
                            .newInstance()
                    val result = obj
                            .declaredMethods
                            .first { method -> method.name == methodName }
                            .invoke(objInstance, it) as String?
                    it.json(Response(if (result == null) 0 else -1, result))
                } catch (ex: Exception) {
                    it.json(Response(-1, "Error found method: ${ex.message}"))
                }
            }

            get("/cont") {
                kopycat.start()
                it.json(Response(0, "Target run"))
            }

            get("/halt") {
                kopycat.halt()
                it.json(Response(0, "Target halt"))
            }

            get("/step") {
                val result = kopycat.step()
                it.json(Response(result.toInt(), "Target step"))
            }

            get("/add_segment") { TODO() }
            get("/clear_mmu") { TODO() }
            get("/add_memory_translator") { TODO() }

        }
    }

    init {
        app.initializeKopycatREST()
        log.info { "Starting Javalin RESTful API on port $port" }
        app.start()
    }
}