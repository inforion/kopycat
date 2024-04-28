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
package ru.inforion.lab403.kopycat.cores.mips.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.cores.mips.enums.MipsCallingConvention
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.MipsCPU
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


class GPRBank(cpu: MipsCPU, abi: MipsCallingConvention = MipsCallingConvention.O64) :
    ARegistersBankNG<MipsCore>("CPU General Purpose Registers", 32, cpu.BIT_DEPTH.bits) {
    // renamed for GDB trace
    val zero = object : Register("\$r0", 0, dtype = cpu.BIT_DEPTH) {
        override var value: ULong
            get() = 0u
            set(value) = Unit
    }

    val at = Register("\$at", 1, dtype = cpu.BIT_DEPTH)

    val v0 = Register("\$v0", 2, dtype = cpu.BIT_DEPTH)
    val v1 = Register("\$v1", 3, dtype = cpu.BIT_DEPTH)

    val a0 = Register("\$a0", 4, dtype = cpu.BIT_DEPTH)
    val a1 = Register("\$a1", 5, dtype = cpu.BIT_DEPTH)
    val a2 = Register("\$a2", 6, dtype = cpu.BIT_DEPTH)
    val a3 = Register("\$a3", 7, dtype = cpu.BIT_DEPTH)

    // TODO: ugly fix
    val t0 = Register(if (abi == MipsCallingConvention.O64) "\$t0" else "\$a4", 8,  dtype = cpu.BIT_DEPTH)
    val t1 = Register(if (abi == MipsCallingConvention.O64) "\$t1" else "\$a5", 9,  dtype = cpu.BIT_DEPTH)
    val t2 = Register(if (abi == MipsCallingConvention.O64) "\$t2" else "\$a6", 10, dtype = cpu.BIT_DEPTH)
    val t3 = Register(if (abi == MipsCallingConvention.O64) "\$t3" else "\$a7", 11, dtype = cpu.BIT_DEPTH)
    val t4 = Register(if (abi == MipsCallingConvention.O64) "\$t4" else "\$t0", 12, dtype = cpu.BIT_DEPTH)
    val t5 = Register(if (abi == MipsCallingConvention.O64) "\$t5" else "\$t1", 13, dtype = cpu.BIT_DEPTH)
    val t6 = Register(if (abi == MipsCallingConvention.O64) "\$t6" else "\$t2", 14, dtype = cpu.BIT_DEPTH)
    val t7 = Register(if (abi == MipsCallingConvention.O64) "\$t7" else "\$t3", 15, dtype = cpu.BIT_DEPTH)


    val s0 = Register("\$s0", 16, dtype = cpu.BIT_DEPTH)
    val s1 = Register("\$s1", 17, dtype = cpu.BIT_DEPTH)
    val s2 = Register("\$s2", 18, dtype = cpu.BIT_DEPTH)
    val s3 = Register("\$s3", 19, dtype = cpu.BIT_DEPTH)
    val s4 = Register("\$s4", 20, dtype = cpu.BIT_DEPTH)
    val s5 = Register("\$s5", 21, dtype = cpu.BIT_DEPTH)
    val s6 = Register("\$s6", 22, dtype = cpu.BIT_DEPTH)
    val s7 = Register("\$s7", 23, dtype = cpu.BIT_DEPTH)

    val t8 = Register("\$t8", 24, dtype = cpu.BIT_DEPTH)
    val t9 = Register("\$t9", 25, dtype = cpu.BIT_DEPTH)

    val k0 = Register("\$k0", 26, dtype = cpu.BIT_DEPTH)
    val k1 = Register("\$k1", 27, dtype = cpu.BIT_DEPTH)

    val gp = Register("\$gp", 28, dtype = cpu.BIT_DEPTH)
    val sp = Register("\$sp", 29, dtype = cpu.BIT_DEPTH)

    val fp = Register( if (abi == MipsCallingConvention.O64) "\$fp" else "\$s8", 30, dtype = cpu.BIT_DEPTH)
    val ra = Register("\$ra", 31, dtype = cpu.BIT_DEPTH)
}
