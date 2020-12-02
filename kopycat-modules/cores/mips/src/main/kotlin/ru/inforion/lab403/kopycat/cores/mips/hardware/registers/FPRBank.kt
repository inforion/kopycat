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
package ru.inforion.lab403.kopycat.cores.mips.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class FPRBank : ARegistersBankNG<MipsCore>("FPU General Purpose Registers", 32,  64) {
    val f0 = Register("f0", 0)
    val f1 = Register("f1", 1)
    val f2 = Register("f2", 2)
    val f3 = Register("f3", 3)
    val f4 = Register("f4", 4)
    val f5 = Register("f5", 5)
    val f6 = Register("f6", 6)
    val f7 = Register("f7", 7)
    val f8 = Register("f8", 8)
    val f9 = Register("f9", 9)
    val f10 = Register("f10", 10)
    val f11 = Register("f11", 11)
    val f12 = Register("f12", 12)
    val f13 = Register("f13", 13)
    val f14 = Register("f14", 14)
    val f15 = Register("f15", 15)
    val f16 = Register("f16", 16)
    val f17 = Register("f17", 17)
    val f18 = Register("f18", 18)
    val f19 = Register("f19", 19)
    val f20 = Register("f20", 20)
    val f21 = Register("f21", 21)
    val f22 = Register("f22", 22)
    val f23 = Register("f23", 23)
    val f24 = Register("f24", 24)
    val f25 = Register("f25", 25)
    val f26 = Register("f26", 26)
    val f27 = Register("f27", 27)
    val f28 = Register("f28", 28)
    val f29 = Register("f29", 29)
    val f30 = Register("f30", 30)
    val f31 = Register("f31", 31)
}