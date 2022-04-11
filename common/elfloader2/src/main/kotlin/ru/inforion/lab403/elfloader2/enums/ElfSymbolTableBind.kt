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


 
enum class ElfSymbolTableBind(val low: UInt, val high: UInt = low) {
    STB_LOCAL(0u),
    STB_GLOBAL(1u),
    STB_WEAK(2u),
    STB_PROC(13u, 15u);

    val shortName get() = name.removePrefix("STB_")
    val range = low..high

    companion object {
        fun UInt.elfSymbolTableBind(onFail: (UInt) -> ElfSymbolTableBind) = values().find { this in it.range } ?: onFail(this)
    }
}