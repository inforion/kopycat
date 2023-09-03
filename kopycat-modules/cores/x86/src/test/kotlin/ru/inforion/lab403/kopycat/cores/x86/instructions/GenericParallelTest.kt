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
package ru.inforion.lab403.kopycat.cores.x86.instructions

import ru.inforion.lab403.common.extensions.bigintByHex
import ru.inforion.lab403.common.extensions.ulongByHex
import ru.inforion.lab403.common.extensions.unhexlify
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.LongDouble
import java.math.BigInteger

internal class GenericParallelTest(private val conditions: List<Iterable<Condition>>) {
    constructor(vararg condition: Iterable<Condition>) : this(condition.asList())

    interface Condition {
        fun apply(parallel: Parallel)
    }

    class Reg(private val r: x86GPR, private val value: ULong) : Condition {
        override fun apply(parallel: Parallel) {
            parallel.test.x86.cpu.regs.gpr(r, Datatype.QWORD).value = value
            // Для unicorn применится при вызове sync
        }
    }

    class XMM(private val i: Int, private val value: BigInteger) : Condition {
        constructor(i: Int, hexString: String) : this(i, BigInteger(hexString, 16))

        override fun apply(parallel: Parallel) {
            parallel.test.x86.sse.xmm[i] = value
            // Для unicorn применится при входе в sync
        }
    }

    class x87(private val i: Int, private val value: BigInteger) : Condition {
        constructor(i: Int, hexString: String) : this(i, hexString.bigintByHex)
        constructor(i: Int, longDouble: LongDouble) : this(i, longDouble.ieee754AsUnsigned())

        override fun apply(parallel: Parallel) {
            // NOTE: осторожно, оно не трогает указатель на вершину стека
            parallel.test.x86.fpu.st(i, value)
            // Для unicorn применится при входе в sync
        }
    }

    class MMX(private val i: Int, private val value: ULong) : Condition {
        constructor(i: Int, hexString: String) : this(i, hexString.ulongByHex)

        override fun apply(parallel: Parallel) {
            parallel.test.x86.fpu.mmx(i, value)
            // Для unicorn применится при входе в sync
        }
    }

    class Mem(val addr: ULong, val data: ByteArray) : Condition {
        constructor(addr: ULong, data: String) : this(addr, data.unhexlify())
        override fun apply(parallel: Parallel) {
            parallel.store(addr, data)
        }
    }

    fun test(test: AX86InstructionTest, unicorn: UnicornEmu, block: Parallel.() -> Unit) {
        for (testcase in conditions) {
            parallel(test, unicorn) {
                testcase.forEach { it.apply(this) }
                sync()
                block()
                assert()
                testcase.forEach {
                    if (it is Mem) {
                        assertMem(it.addr, it.data.size)
                    }
                }
            }
        }
    }
}
