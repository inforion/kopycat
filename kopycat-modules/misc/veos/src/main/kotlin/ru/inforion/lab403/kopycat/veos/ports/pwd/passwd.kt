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
package ru.inforion.lab403.kopycat.veos.ports.pwd

import ru.inforion.lab403.kopycat.veos.api.pointers.StructPointer
import ru.inforion.lab403.kopycat.veos.kernel.System

class passwd(sys: System, address: Long) : StructPointer(sys, address) {
    companion object {
        const val sizeOf = 0x1C

        fun nullPtr(sys: System) = passwd(sys, 0)

        fun allocate(sys: System) = passwd(sys, sys.allocateClean(sizeOf))
    }

    var pw_name by pointer(0x00)
    var pw_passwd by pointer(0x04)

    var pw_uid by int(0x08)
    var pw_gid by int(0x0C)

    var pw_gecos by pointer(0x10)
    var pw_dir by pointer(0x14)
    var pw_shell by pointer(0x18)
}