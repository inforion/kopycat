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
package ru.inforion.lab403.kopycat.veos.ports.time

import ru.inforion.lab403.kopycat.veos.api.pointers.StructPointer
import ru.inforion.lab403.kopycat.veos.kernel.System

class tm(sys: System, address: Long) : StructPointer(sys, address) {
    companion object {
        const val sizeOf = 0x24

        fun nullPtr(sys: System) = tm(sys, 0)

        fun allocate(sys: System) = tm(sys, sys.allocateClean(sizeOf))
    }

    var tm_sec by int(0x00) /* seconds */
    var tm_min by int(0x04) /* minutes */
    var tm_hour by int(0x08) /* hours */

    var tm_mday by int(0x0C) /* day of the month */
    var tm_mon by int(0x10) /* month */
    var tm_year by int(0x14) /* year */

    var tm_wday by int(0x18) /* day of the week */
    var tm_yday by int(0x1C) /* day in the year */
    var tm_isdst by int(0x20) /* daylight saving time */
}