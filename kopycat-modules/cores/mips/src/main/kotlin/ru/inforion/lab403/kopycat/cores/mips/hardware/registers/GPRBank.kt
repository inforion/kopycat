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


class GPRBank : ARegistersBankNG<MipsCore>("CPU General Purpose Registers", 32, 32) {

    val zero = object : Register( "\$zero", 0) {
        override var value: Long
            get() = 0
            set(value) = Unit
    }

    val at = Register("\$at", 1)

    val v0 = Register("\$v0", 2)
    val v1 = Register("\$v1", 3)

    val a0 = Register("\$a0", 4)
    val a1 = Register("\$a1", 5)
    val a2 = Register("\$a2", 6)
    val a3 = Register("\$a3", 7)

    val t0 = Register("\$t0", 8)
    val t1 = Register("\$t1", 9)
    val t2 = Register("\$t2", 10)
    val t3 = Register("\$t3", 11)
    val t4 = Register("\$t4", 12)
    val t5 = Register("\$t5", 13)
    val t6 = Register("\$t6", 14)
    val t7 = Register("\$t7", 15)

    val s0 = Register("\$s0", 16)
    val s1 = Register("\$s1", 17)
    val s2 = Register("\$s2", 18)
    val s3 = Register("\$s3", 19)
    val s4 = Register("\$s4", 20)
    val s5 = Register("\$s5", 21)
    val s6 = Register("\$s6", 22)
    val s7 = Register("\$s7", 23)

    val t8 = Register("\$t8", 24)
    val t9 = Register("\$t9", 25)

    val k0 = Register("\$k0", 26)
    val k1 = Register("\$k1", 27)

    val gp = Register("\$gp", 28)
    val sp = Register("\$sp", 29)
    val fp = Register("\$fp", 30)
    val ra = Register("\$ra", 31)
}
