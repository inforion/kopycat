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
package ru.inforion.lab403.kopycat.modules.cores.device.hardware

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.extensions.hexAsULong
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACPU
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.DecoderException
import ru.inforion.lab403.kopycat.modules.cores.device.TestCore
import ru.inforion.lab403.kopycat.modules.cores.device.instructions.INSN
import ru.inforion.lab403.kopycat.modules.cores.device.instructions.*
import ru.inforion.lab403.kopycat.modules.cores.device.operands.TestInstruction
import ru.inforion.lab403.kopycat.modules.cores.device.registers.TestGPR
import ru.inforion.lab403.kopycat.modules.cores.device.registers.TestGPRBankNG

class TestCPU(val testCore: TestCore, name: String): ACPU<TestCPU, TestCore, TestInstruction, TestGPR>(testCore, name) {
    override fun reg(index: Int): Long = regs[index].value
    override fun reg(index: Int, value: Long) {
        regs[index].value = value
    }
    override fun count() = regs.count()
    override fun stringify(): String = "test cpu"

    override var pc: Long
        get() = regs.pc.value
        set(value) { regs.pc.value = value }

    val regs = TestGPRBankNG()

    override fun decode() {
        val calc = core as TestCore

        val ea = regs.pc.value

        val opcode = core.read(Datatype.DWORD, ea, 0)

        insn = when (opcode) {
            INSN.ADD -> Add(calc)
            INSN.MOV -> Mov(calc)
            INSN.MUL -> Mul(calc)
            INSN.SUB -> Sub(calc)
            INSN.INF -> Inf(calc)
            else -> throw DecoderException(opcode, ea)
        }
    }

    override fun execute(): Int {
        insn.execute()
        regs.pc.value = regs.pc.value + insn.size
        return 1
    }

    override fun reset() {
        regs.reset()
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super.serialize(ctxt) + mapOf(
                "pc" to pc.hex8,
                "regs" to regs.serialize(ctxt)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        pc = (snapshot["pc"] as String).hexAsULong
        regs.deserialize(ctxt, snapshot["regs"] as Map<String, String>)
    }
}