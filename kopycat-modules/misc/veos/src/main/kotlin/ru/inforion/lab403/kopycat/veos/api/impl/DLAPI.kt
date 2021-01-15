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
package ru.inforion.lab403.kopycat.veos.api.impl

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.extensions.toFile
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.API
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIFunction
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult


/**
 *
 * Implementation of dlfcn.h of dynamic linking loader library
 */
class DLAPI(os: VEOS<*>) : API(os) {

    // POSIX.1-2001
    // https://linux.die.net/man/3/dlopen
    val dlopen = object : APIFunction("dlopen") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Int)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val (filename, flag) = argv
            val filenameString = os.sys.readAsciiString(filename)
            if (filenameString[0] == '/') {
                if (!os.filesystem.exists(filenameString))
                    return retval(0) // TODO: error processing

                val fullPath = os.filesystem.absolutePath(filenameString)
                os.loader.loadLibrary(fullPath)
                val module = fullPath.toFile().name
                val result = os.loader.moduleAddress(module)
                return retval(result)
            } else TODO("I have no LD_LIBRARY_PATH and etc...")
        }
    }

    // POSIX.1-2001
    // https://linux.die.net/man/3/dlsym
    val dlsym = object : APIFunction("dlsym") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            val (handle, symbol) = argv
            val symbolString = os.sys.readAsciiString(symbol)

            val module = os.loader.moduleName(handle)

            log.fine { "[0x${ra.hex8}] dlsym($module, $symbol)" }

            val result = os.loader.findSymbol(module, symbolString)?.address ?: 0L
            check (result != 0L) { "Symbol $symbolString not found in $module" }
            return retval(result)
        }
    }

    // POSIX.1-2001
    // https://linux.die.net/man/3/derror
    val dlerror = object : APIFunction("dlerror") {
        override val args = arrayOf<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            log.config { "[0x${ra.hex8}] dlerror() is not implemented!" }
            return retval(0L)
        }
    }

    // POSIX.1-2001
    // https://linux.die.net/man/3/dlclose
    val dlclose = object : APIFunction("dlclose") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            log.config { "[0x${ra.hex8}] dlclose() is not implemented!" }
            return retval(0L)
        }
    }
}