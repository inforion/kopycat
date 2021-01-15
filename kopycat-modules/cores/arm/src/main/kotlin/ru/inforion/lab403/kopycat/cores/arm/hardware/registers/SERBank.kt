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
package ru.inforion.lab403.kopycat.cores.arm.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


class SERBank : ARegistersBankNG<AARMCore>("Security Extension Registers", 6, 32) {

    val isr = Register("isr", 0)
    val mvbar = Register("mvbar", 1)

    inner class NSACR : Register("nsacr", 2) {
        var rfr by bitOf(19)
    }

    val nsacr = NSACR()

    inner class SCR : Register("scr", 3) {
        var ns by bitOf(0)
        var irq by bitOf(1)
        var fiq by bitOf(2)
        var ea by bitOf(3)
        var fw by bitOf(4)
        var aw by bitOf(5)
        var net by bitOf(6)
        var scd by bitOf(7)
        var hce by bitOf(8)
        var sif by bitOf(9)
    }

    val scr = SCR()

    val sder = Register("sder", 4)
    val vbar = Register("vbar", 5)
}