/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.elfloader2.enums

import ru.inforion.lab403.common.extensions.*



enum class ElfType(val low: UShort, val high: UShort = low) {
    ET_NONE(0u),
    ET_REL(1u),
    ET_EXEC(2u),
    ET_DYN(3u),
    ET_CORE(4u),
    ET_PROC(0xFF00u, 0xFFFFu);

    val range = low..high
    val shortName get() = name.removePrefix("ET_")

    companion object {
        fun getNameById(id: UShort): String {
            val type = find<ElfType> { it.low == id }
            return if (type != null) type.name else "Unknown ElfType id ${id.hex4}"
        }
        fun cast(id: UShort, onFail: (UShort) -> ElfType) = values().find { id in it.range} ?: onFail(id)
    }
}