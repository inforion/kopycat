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
package ru.inforion.lab403.kopycat.cores.msp430.enums



enum class MSP430GPR(val id: Int, val regName : String) {
    r0(0, "PC"),
    r1(1, "SP"),
    r2(2, "SR"),
    r3(3, "CG"),
    r4(4, "R4"),
    r5(5, "R5"),
    r6(6, "R6"),
    r7(7, "R7"),
    r8(8, "R8"),
    r9(9, "R9"),
    r10(10, "R10"),
    r11(11, "R11"),
    r12(12, "R12"),
    r13(13, "R13"),
    r14(14, "R14"),
    r15(15, "R15");

    companion object {
        val COUNT: Int get() = values().size
        fun from(id: Int): MSP430GPR = MSP430GPR.values().first { it.id == id }
    }
}