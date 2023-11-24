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
package ru.inforion.lab403.kopycat.cores.mips.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class FPRBank(dtype: Datatype = Datatype.DWORD) :
    ARegistersBankNG<MipsCore>("FPU General Purpose Registers", 32, 64) {
    val f0 = Register("f0", 0, dtype = dtype)
    val f1 = Register("f1", 1, dtype = dtype)
    val f2 = Register("f2", 2, dtype = dtype)
    val f3 = Register("f3", 3, dtype = dtype)
    val f4 = Register("f4", 4, dtype = dtype)
    val f5 = Register("f5", 5, dtype = dtype)
    val f6 = Register("f6", 6, dtype = dtype)
    val f7 = Register("f7", 7, dtype = dtype)
    val f8 = Register("f8", 8, dtype = dtype)
    val f9 = Register("f9", 9, dtype = dtype)
    val f10 = Register("f10", 10, dtype = dtype)
    val f11 = Register("f11", 11, dtype = dtype)
    val f12 = Register("f12", 12, dtype = dtype)
    val f13 = Register("f13", 13, dtype = dtype)
    val f14 = Register("f14", 14, dtype = dtype)
    val f15 = Register("f15", 15, dtype = dtype)
    val f16 = Register("f16", 16, dtype = dtype)
    val f17 = Register("f17", 17, dtype = dtype)
    val f18 = Register("f18", 18, dtype = dtype)
    val f19 = Register("f19", 19, dtype = dtype)
    val f20 = Register("f20", 20, dtype = dtype)
    val f21 = Register("f21", 21, dtype = dtype)
    val f22 = Register("f22", 22, dtype = dtype)
    val f23 = Register("f23", 23, dtype = dtype)
    val f24 = Register("f24", 24, dtype = dtype)
    val f25 = Register("f25", 25, dtype = dtype)
    val f26 = Register("f26", 26, dtype = dtype)
    val f27 = Register("f27", 27, dtype = dtype)
    val f28 = Register("f28", 28, dtype = dtype)
    val f29 = Register("f29", 29, dtype = dtype)
    val f30 = Register("f30", 30, dtype = dtype)
    val f31 = Register("f31", 31, dtype = dtype)
}