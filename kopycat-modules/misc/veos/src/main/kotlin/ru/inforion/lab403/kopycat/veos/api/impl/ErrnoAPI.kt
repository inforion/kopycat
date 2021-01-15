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
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.API
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIFunction
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIVariable


/**
 *
 * Implementation of errno.h of C standard library
 */
class ErrnoAPI(os: VEOS<*>) : API(os) {
    // REVIEW: header

    val errno = APIVariable.int(os, "errno")

    // http://refspecs.linux-foundation.org/LSB_4.1.0/LSB-Core-generic/LSB-Core-generic/baselib---errno-location.html
    val __errno_location = object : APIFunction("__errno_location") {
        override val args = emptyArray<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            log.finest { "[0x${ra.hex8}] get_errno_ptr()" }
            val p_errno = errno.allocated.address!!
            return retval(p_errno)
        }
    }
}