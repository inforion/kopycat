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
package ru.inforion.lab403.kopycat.veos.ports.dirent

import ru.inforion.lab403.kopycat.veos.api.pointers.StructPointer
import ru.inforion.lab403.kopycat.veos.kernel.System

class DIR(sys: System, address: Long): StructPointer(sys, address) {
    companion object {
        const val sizeOf = 20

        fun nullPtr(sys: System) = DIR(sys, 0)

        fun allocate(sys: System) = DIR(sys, sys.allocateClean(sizeOf))
    }

    var fd by int(0)

    // TODO: the following offsets may be wrong
    // __libc_lock_define (, lock) /* Mutex lock for this structure.  */

    // FIXME: they are in fact size_t and may not work for 64 bits
    var allocation by int(4)
    var size by int(8)
    var offset by int(12)

    // FIXME: they are in fact off_t and may not work for 64 bits
    var filepos by int(16)


    var errcode by int(20)

    val file get() = sys.filesystem.file(fd)
}