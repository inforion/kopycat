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
package ru.inforion.lab403.kopycat

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.extensions.argparse.parseArguments
import ru.inforion.lab403.common.logging.FINEST
import ru.inforion.lab403.common.logging.Levels
import ru.inforion.lab403.common.logging.logger.Logger
import ru.inforion.lab403.kopycat.consoles.AConsole
import ru.inforion.lab403.kopycat.consoles.Argparse
import ru.inforion.lab403.kopycat.consoles.Kotlin
import ru.inforion.lab403.kopycat.consoles.Python
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.gdbstub.GDBServer
import ru.inforion.lab403.kopycat.interactive.REPL
import ru.inforion.lab403.kopycat.interactive.protocols.ConsoleRestProtocol
import ru.inforion.lab403.kopycat.interactive.protocols.KopycatRestProtocol
import ru.inforion.lab403.kopycat.interactive.protocols.RegistryRestProtocol
import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry
import kotlin.system.exitProcess


object KopycatStarter {
    @Transient val log = logger(FINEST)

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

    private fun console(kopycat: Kopycat, opts: Options): AConsole = if (opts.kts) {
        Kotlin(kopycat)
                .takeIf { it.initialized() }
                .sure { "-kts option specified but Kotlin console can't be initialized!" }
    } else {
        log.warning { "Use -kts option to enable Kotlin console. In the next version Kotlin console will be default." }

        Python(kopycat, opts.python).takeIf { it.initialized() }
                ?: Kotlin(kopycat).takeIf { it.initialized() }
                ?: Argparse(kopycat)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val version = System.getProperty("java.version")
        log.info { "Build version: ${buildInformationString()} [JRE v$version]" }

        val opts = args.parseArguments<Options>()

        opts.loggingLevel?.loggerConfigure()

        val fullRegistriesPaths = getRegistryPath(opts.registriesPaths)

        val registry = ModuleLibraryRegistry.create(fullRegistriesPaths, opts.userModulesPath)

        val kopycat = Kopycat(registry).also { it.setSnapshotsDirectory(opts.snapshotsDir) }

        if (opts.modulesRegistryAllInfo) {
            kopycat.printModulesRegistryInfo(false)
            exitProcess(0)
        }

        if (opts.modulesRegistryTopInfo) {
            kopycat.printModulesRegistryInfo(true)
            exitProcess(0)
        }

        val gdb = opts.gdbPort?.let {
            GDBServer(it, true, opts.gdbBinaryProto).also { gdb ->
                log.info { "$gdb was created" }
            }
        }

        val name = opts.name
        val library = opts.library

        if (name != null && library != null) {
            kopycat.runCatching {
                open(name, library, opts.snapshot, opts.parameters, false, gdb)
            }.onFailure {
                it.printStackTrace()
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

        val console = console(kopycat, opts)
        log.info { "${console.name} console enabled" }

        opts.restPort?.let {
            val modules = mutableListOf<Module>()
            JavalinServer(it,
                    KopycatRestProtocol(kopycat, modules),
                    RegistryRestProtocol(kopycat.registry, modules),
                    ConsoleRestProtocol(console))
        }

        REPL(console)
    }
}

