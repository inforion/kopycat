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
package ru.inforion.lab403.kopycat.cores.x86.enums



enum class x86GPR(val id: Int, val n8: String, val n16: String, val n32: String) {
    EAX(0, "al", "ax", "eax"),
    ECX(1, "cl", "cx", "ecx"),
    EDX(2, "dl", "dx", "edx"),
    EBX(3, "bl", "bx", "ebx"),
    ESP(4, "ah", "sp", "esp"),
    EBP(5, "ch", "bp", "ebp"),
    ESI(6, "dh", "si", "esi"),
    EDI(7, "bh", "di", "edi"),

    EIP(8, "??", "ip", "eip"),

    NONE(9, "noneB", "noneW", "noneD");

    companion object {
        val COUNT: Int get() = values().size
        fun from(id: Int): x86GPR = x86GPR.values().first { it.id == id }
        fun fromOrNull(name: String): x86GPR? = values().firstOrNull { it.name == name }
    }
}