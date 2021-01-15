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

import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.API
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIFunction
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult

/**
 *
 * Implementation of some C++ routines
 */
class CXXAPI(os: VEOS<*>) : API(os) {
    val __cxa_guard_acquire = object : APIFunction("__cxa_guard_acquire") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            // Returns 1 if the caller needs to run the initializer and then either
            // call __cxa_guard_release() or __cxa_guard_abort().  If zero is returned,
            // then the initializer has already been run.
            val p_guard_object = argv[0]
            var guard_value = os.abi.readInt(p_guard_object)
            return if (guard_value != 1L) {
                guard_value = 1L
                os.abi.writeInt(p_guard_object, guard_value)
                retval(guard_value)
            } else retval(0)
        }
    }

    val __cxa_guard_release = object : APIFunction("__cxa_guard_release") {
        override val args = arrayOf(ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            // Sets the first byte of the guard_object to a non-zero value.
            // Releases any locks acquired by __cxa_guard_acquire().
            val p_guard_object = argv[0]
            os.abi.writeChar(p_guard_object, 0)
            return void()
        }
    }

    val __cxa_atexit = object : APIFunction("__cxa_guard_release") {
        override val args = arrayOf(ArgType.Pointer, ArgType.Pointer, ArgType.Pointer)
        override fun exec(name: String, vararg argv: Long): APIResult {
            log.severe { "__cxa_atexit(...) not implemented" }
            return retval(0)
        }
    }
}