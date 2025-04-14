/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
package ru.inforion.lab403.kopycat

import ru.inforion.lab403.common.argparse.parseArguments
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.javalin.JavalinServer
import ru.inforion.lab403.common.jline.jline
import ru.inforion.lab403.common.jline.waitUntilReturn
import ru.inforion.lab403.common.logging.FINEST
import ru.inforion.lab403.common.logging.formatters.NotInformative
import ru.inforion.lab403.common.logging.logStackTrace
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.logging.loggerConfigure
import ru.inforion.lab403.common.logging.publishers.FileBeautyPublisher
import ru.inforion.lab403.common.logging.publishers.WriterBeautyPublisher
import ru.inforion.lab403.common.logging.storage.LoggerStorage
import ru.inforion.lab403.common.wsrpc.WebSocketRpcServer
import ru.inforion.lab403.kopycat.consoles.AConsole
import ru.inforion.lab403.kopycat.consoles.Kotlin
import ru.inforion.lab403.kopycat.consoles.Python
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.gdbstub.GDBServer
import ru.inforion.lab403.kopycat.interactive.REPL
import ru.inforion.lab403.kopycat.interactive.protocols.KopycatRestProtocol
import ru.inforion.lab403.kopycat.interactive.protocols.RegistryRestProtocol
import ru.inforion.lab403.kopycat.interactive.wsrpc.KopycatEndpoint
import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry
import java.io.File
import java.io.Writer
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.isDirectory
import kotlin.system.exitProcess


object KopycatStarter {
    @Transient
    val log = logger(FINEST)

    /**
     * {EN}
     * Construct and returns full registry path using default modules directory (home directory of emulator with
     * postfix [settings.modulesDirectoryPath]) and user specified [optional] directory
     * {EN}
     */
    private fun getRegistryPath(optional: String?): String? {
        val kopycatHome = Kopycat.getHomeDir()
        log.info { "Kopycat directory: '$kopycatHome'" }
        val default = kopycatHome / settings.modulesDirectoryPath
        return if (optional != null) "$default,$optional" else default
    }

    private fun console(kopycat: Kopycat, opts: Options): AConsole? = if (opts.kts) {
        Kotlin(kopycat)
            .takeIf { it.initialized() }
            .sure { "-kts option specified but Kotlin console can't be initialized!" }
    } else {
        log.warning { "Use -kts option to enable Kotlin console. In the next version Kotlin console will be default." }

        Python(kopycat, opts.python).takeIf { it.initialized() }
            ?: Kotlin(kopycat).takeIf { it.initialized() }
    }

    var initScript: File? = null

    @JvmStatic
    fun main(args: Array<String>) {
        LoggerStorage.removePublisher(LoggerStorage.ALL, LoggerStorage.defaultPublisher)

        val writer = object : Writer() {
            var backend: Writer = System.out.writer()
            override fun close() = backend.close()
            override fun flush() = backend.flush()
            override fun write(cbuf: CharArray, off: Int, len: Int) = backend.write(cbuf, off, len)
        }

        WriterBeautyPublisher(
            "kc-stdout",
            writer,
            flushEnabled = true,
            formatter = NotInformative()
        ).let {
            LoggerStorage.addPublisher(LoggerStorage.ALL, it)
        }

        log.info { "Build version: ${buildInformationString()} [JRE v$javaVersion]" }

        val opts = args.parseArguments<Options>()

        opts.loggingLevel?.loggerConfigure()
        opts.loggingFile?.let { Path(it) }?.also { path ->
            if (!path.parent.isDirectory()) {
                log.severe { "Log file path ${path.parent} was created" }
                path.parent.createDirectories()
            }

            FileBeautyPublisher(
                "kc-file-publisher",
                path.toString(),
                append = true,
                formatter = NotInformative()
            ).let {
                LoggerStorage.addPublisher(LoggerStorage.ALL, it)
            }
            log.info { "Set additional log file: '${path}'" }
        }

        val fullRegistriesPaths = getRegistryPath(opts.registriesPaths)

        val registry = ModuleLibraryRegistry.create(fullRegistriesPaths, opts.userModulesPath)

        log.info { "registry='${registry.regCfgLine}' libraries='${registry.libCfgLine}'" }

        val kopycat = Kopycat(registry).also {
            it.setSnapshotsDirectory(opts.snapshotsDir)
            it.setResourceDirectory(opts.resourceDir)
            it.setScriptDirectory(opts.scriptDir)
        }

        initScript = opts.initScript
            ?.let { (Path(Kopycat.scriptDir) / Path(it)).toFile() }
            ?.let {
                return@let if (it.exists()) {
                    it
                } else {
                    log.severe { "Initial script at path ${it.path} does not exist" }
                    null
                }
            }

        val historyFilePath = opts.historyFile
            ?.let { Path(Kopycat.getWorkingDir(it)) }
            ?.also { path ->
                if (!path.parent.isDirectory()) {
                    log.severe { "History command path ${path.parent} was created" }
                    path.parent.createDirectories()
                }
            }

        if (opts.modulesRegistryAllInfo) {
            kopycat.printModulesRegistryInfo(false)
            exitProcess(0)
        }

        if (opts.modulesRegistryTopInfo) {
            kopycat.printModulesRegistryInfo(true)
            exitProcess(0)
        }

        val gdb = opts.gdbPort ifItNotNull { port ->
            GDBServer(
                port,
                opts.gdbHost ?: "0.0.0.0",
                opts.gdbPacketSize,
                opts.gdbBinaryProto
            )
        }

        val name = opts.name
        val library = opts.library

        if (name != null) {
            kopycat.runCatching {
                open(name, library, opts.snapshot, opts.parameters, gdb, opts.traceable)
            }.onFailure {
                it.logStackTrace(log)
                if (opts.standalone)
                    exitProcess(-1)
            }
        }

        if (opts.connectionsInfo)
            kopycat.printModulesConnectionsInfo()

        if (opts.portsWarnings)
            kopycat.printModulesPortsWarnings()

        require(!opts.run || !opts.standalone) { "You should specify only -run or -standalone not both" }

        if (opts.run)
            kopycat.start()

        if (opts.standalone) {
            // log.info { "Standalone mode is activated" }
            kopycat.start { exitProcess(0) } // TODO: get from guest
        }

        /* val javalin = */ opts.restPort ifItNotNull { port ->
            val modules = mutableListOf<Module>()
            JavalinServer(
                port,
                KopycatRestProtocol(kopycat, modules)::apply,
                RegistryRestProtocol(kopycat.registry, modules)::apply,
            )
        }

        opts.rpcAddress ifItNotNull {
            val address = it.toInetSocketAddress()
            WebSocketRpcServer(address.hostName, address.port).apply {
                register(KopycatEndpoint(kopycat))
                start()
            }
        }

        console(kopycat, opts) ifNotNull {
            log.info { "${this.name} console enabled" }

            REPL.create(
                this,
                initScript,
                historyFilePath
            ) {
                writer.backend = it
            }

            // if (javalin != null) {
                // ConsoleRestProtocol(this).apply(javalin)
            // }
        } otherwise {
            log.warning { "No valid console have been detected" }
            jline().waitUntilReturn()
        }
    }
}

