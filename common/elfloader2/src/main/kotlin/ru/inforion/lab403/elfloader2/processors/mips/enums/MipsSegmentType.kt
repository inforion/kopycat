/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
package ru.inforion.lab403.elfloader2.processors.mips.enums

import ru.inforion.lab403.common.extensions.*


enum class MipsSegmentType(val id: UInt) {
    PT_MIPS_REGINFO     (0x70000000u),       // Register usage information
    PT_MIPS_RTPROC      (0x70000001u),
    PT_MIPS_OPTIONS	    (0x70000002u),
    PT_MIPS_ABIFLAGS    (0x70000003u);       // Records ABI related flags

    companion object {
        fun getNameById(id: UInt): String {
            val st = find<MipsSegmentType> { it.id == id }
            return if (st != null) st.name else "Unknown MIPS segment type id ${id.hex8}"
        }
    }
}