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

import ru.inforion.lab403.common.extensions.argparse.ApplicationOptions
import ru.inforion.lab403.common.extensions.argparse.choices
import ru.inforion.lab403.common.extensions.argparse.flag
import ru.inforion.lab403.common.extensions.argparse.variable
import ru.inforion.lab403.common.logging.ALL
import ru.inforion.lab403.common.logging.Levels
import ru.inforion.lab403.common.logging.LogLevel
import ru.inforion.lab403.common.logging.logLevel
import java.util.logging.Level

class Options : ApplicationOptions("kopycat", "virtualization platform") {
    val userModulesPath: String? by variable("-u", "--modules",
            "Modules libraries paths in format: lib1:path/to/lib1,lib2:path/to/lib2")
    val registriesPaths: String? by variable("-y", "--registry", "Path to registry with libraries")

    val name: String? by variable("-n", "--name",
            "Top instance module name (with package path dot-separated)")
    val library: String? by variable("-l", "--library",
            "Top instance module library name")

    val snapshot: String? by variable("-s", "--snapshot",
            "Snapshot file (top instance module/library can be obtained from here)")

    val parameters: String? by variable("-p", "--parameters",
            "Parameters for top module constructor in format: arg1=100,arg2=/dev/io")

    val snapshotsDir: String? by variable("-w", "--snapshots-dir",
            "Snapshots directory path (default path to store and load snapshots)")

    val gdbPort: Int? by variable("-g", "--gdb-port",
            "GDB server port (if not specified then not started)")
    val restPort: Int? by variable("-r", "--rest",
            "REST server port. If null - Commander will work")

    val gdbBinaryProto: Boolean by flag("-gb", "--gdb-bin-proto",
            "GDB server enabled binary protocol", false)

    val run: Boolean by flag("-run", "--run", "Run emulation as soon as Kopycat ready")

    val standalone: Boolean by flag("-standalone", "--standalone",
            "Run emulation as soon as Kopycat ready and exit when guest application stops", false)

    val modulesRegistryAllInfo: Boolean by flag("-all", "--modules-registry-all-info",
            "Print all loaded modules info and exit", false)
    val modulesRegistryTopInfo: Boolean by flag("-top", "--modules-registry-top-info",
            "Print top loaded modules info and exit", false)
    val connectionsInfo: Boolean by flag("-ci", "--connections-info",
            "Print hier. top module buses connections info at startup", false)
    val portsWarnings: Boolean by flag("-pw", "--ports-warnings",
            "Print all ports warnings when loading Kopycat module at startup", false)

    val python: String by variable("-python", "--python",
            "Python interpreter command") { "python" }

    val kts: Boolean by flag("-kts", "--kotlin-script",
            "Set REPL to Kotlin script language", false)

    val loggingLevel: String? by variable("-ll", "--log-level",
            "Set messages minimum logging level for specified loggers in format logger0=LEVEL,logger1=LEVEL\n" +
                    "Or for all loggers if no '=' was found in value just logger level, i.e. FINE\n" +
                    "Available levels: ${Levels.values().joinToString()}\n")
}