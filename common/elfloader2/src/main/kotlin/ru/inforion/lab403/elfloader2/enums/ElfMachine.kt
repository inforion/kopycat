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


 
enum class ElfMachine(val id: UShort) {
    EM_NONE(0u),     // No machine
    EM_M32(1u),      // AT&T WE 32100
    EM_SPARC(2u),    // SPARC
    EM_386(3u),      // Intel 80386
    EM_68K(4u),      // Motorola 68000
    EM_88K(5u),      // Motorola 68000
    EM_IAMCU(6u),    // Intel MCU
    EM_860(7u),      // Intel 80860
    EM_MIPS(8u),     // MIPS I Architecture

    EM_PPC(20u),     // PowerPC
    EM_PPC64(21u),   // PowerPC64
    EM_ARM(40u),     // ARM
    EM_X86_64(62u),  // AMD x86-64
    EM_AARCH64(183u);// ARM 64 bit

    val shortName get() = name.removePrefix("EM_")

    companion object {
        fun cast(id: UShort, onFail: (UShort) -> ElfMachine) = values().find { it.id == id } ?: onFail(id)
    }
}