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
package ru.inforion.lab403.kopycat.veos.ports.stat

import ru.inforion.lab403.kopycat.veos.api.pointers.StructPointer
import ru.inforion.lab403.kopycat.veos.kernel.System

class stat64_arm(sys: System, address: Long) : StructPointer(sys, address) {
    companion object {
        const val sizeOf = 0x68

        fun nullPtr(sys: System) = stat64_arm(sys, 0)

        fun allocate(sys: System) = stat64_arm(sys, sys.allocateClean(sizeOf))
    }

//    https://elixir.bootlin.com/linux/v4.7/source/arch/arm/include/uapi/asm/stat.h#L56

    var st_dev by longlong(0x0)

    // pad 4 bytes

    var __st_ino by long(0x0C)
    var st_mode by int(0x10)
    var st_nlink by int(0x14)

    var st_uid by long(0x18)
    var st_gid by long(0x1C)

    var st_rdev by longlong(0x24)

    // pad 4 bytes

    var st_size by longlong(0x30)
    var st_blksize by long(0x38)
    var st_blocks by longlong(0x40)

    var st_atime by long(0x48)
    var st_mtime by long(0x50)
    var st_ctime by long(0x58)

    var st_ino by longlong(0x60)
}