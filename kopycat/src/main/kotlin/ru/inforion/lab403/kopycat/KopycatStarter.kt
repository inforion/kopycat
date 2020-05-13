package ru.inforion.lab403.kopycat

import net.sourceforge.argparse4j.inf.Namespace
import net.sourceforge.argparse4j.internal.HelpScreenException
import ru.inforion.lab403.common.extensions.argparser
import ru.inforion.lab403.common.extensions.buildInformationString
import ru.inforion.lab403.common.extensions.flag
import ru.inforion.lab403.common.extensions.variable
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.consoles.AConsole
import ru.inforion.lab403.kopycat.consoles.Argparse
import ru.inforion.lab403.kopycat.consoles.Python
import ru.inforion.lab403.kopycat.gdbstub.GDBServer
import ru.inforion.lab403.kopycat.interactive.REPL
import ru.inforion.lab403.kopycat.interactive.REST
import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry
import java.io.File
import java.util.logging.Level
import kotlin.system.exitProcess


class KopycatStarter {
    companion object {
        val log = logger(Level.FINEST)

        @JvmStatic
        fun main(args: Array<String>) {
            val version = System.getProperty("java.version")
            val kopycatHome = System.getenv("KOPYCAT_HOME")

            log.info { "Java version: $version" }

            log.info { "KOPYCAT_HOME=$kopycatHome" }
            log.info { "Working Directory: ${System.getProperty("user.dir")}" }

            log.info { "Build version information: ${buildInformationString()}" }

            val parser = argparser("kopycat", description = "virtualization platform").apply {
                variable<Int>("-r", "--rest", required = false, help = "REST server port. If null - Commander will work")
                variable<Int>("-g", "--gdb-port", required = false, help = "GDB server port (if not specified then not started)")
                flag("-gb", "--gdb-bin-proto", help = "GDB server enabled binary protocol")
                variable<String>("-n", "--name", required = false, help = "Top instance module name (with package path dot-separated)")
                variable<String>("-l", "--library", required = false, help = "Top instance module library name")
                variable<String>("-s", "--snapshot", required = false, help = "Snapshot file (top instance module/library can be obtained from here)")
                variable<String>("-u", "--modules", required = false, help = "Modules libraries paths in format: lib1:path/to/lib1,lib2:path/to/lib2")
                variable<String>("-y", "--registry", required = false, help = "Path to registry with libraries")
                variable<String>("-p", "--parameters", required = false, help = "Parameters for top module constructor in format: arg1=100,arg2=/dev/io")
                variable( "--python", default = "python", help = "Python interpreter command")
                flag("-pw", "--ports-warnings", help = "Print all ports warnings when loading Kopycat module at startup")
                flag("-all", "--modules-registry-all-info", help = "Print all loaded modules info and exit")
                flag("-top", "--modules-registry-top-info", help = "Print top loaded modules info and exit")
                flag("-ci", "--connections-info", help = "Print hier. top module buses connections info at startup")
            }

            val options: Namespace = try{
                parser.parseArgs(args)
            } catch (ex: HelpScreenException) {
                exitProcess(0)
            }

            val defaultRegistryPath = if (kopycatHome != null) File(kopycatHome, "modules").path else null

            val userModulesPath: String? = options["modules"]
            val registriesPaths: String? = options["registry"]

            val fullRegistriesPaths = if (registriesPaths != null && defaultRegistryPath != null)
                "$defaultRegistryPath,$registriesPaths" else registriesPaths ?: defaultRegistryPath

            val registry = ModuleLibraryRegistry.create(fullRegistriesPaths, userModulesPath)

            val kopycat = Kopycat(registry, "temp")

            val name: String? = options["name"]
            val library: String? = options["library"]
            val snapshot: String? = options["snapshot"]
            val parameters: String? = options["parameters"]
            val gdbPort: Int? = options["gdb_port"]
            val restPort: Int? = options["rest"]
            val modulesRegistryAllInfo: Boolean = options["modules_registry_all_info"] ?: false
            val modulesRegistryTopInfo: Boolean = options["modules_registry_top_info"] ?: false
            val connectionsInfo: Boolean = options["connections_info"] ?: false
            val portsWarnings: Boolean = options["ports_warnings"] ?: false
            val python: String = options["python"]

            if (modulesRegistryAllInfo) {
                kopycat.printModulesRegistryInfo(false)
                exitProcess(0)
            }

            if (modulesRegistryTopInfo) {
                kopycat.printModulesRegistryInfo(true)
                exitProcess(0)
            }

            if (name == null || library == null) {
                log.severe { "Top module name (-n) and top module library (-l) must be specified explicitly!" }
                exitProcess(-1)
            }

            val gdb = if (gdbPort != null) {
                val gdbBinaryProto: Boolean = options["gdb_bin_proto"] ?: false
                GDBServer(gdbPort, true, gdbBinaryProto)
            } else null

            if (gdb != null) log.info { "$gdb was created" }

            kopycat.open(name, library, snapshot, parameters, false, gdb)

            if (connectionsInfo)
                kopycat.printModulesConnectionsInfo()

            if (portsWarnings)
                kopycat.printModulesPortsWarnings()

            var console: AConsole = Python(kopycat, python)
            if (!console.initialized()) {
                log.warning { "Loading Python CLI failed using embedded one..." }
                console = Argparse(kopycat)
            }

            if (restPort != null)
                REST(restPort, console, kopycat)

            REPL(console)
        }
    }
}