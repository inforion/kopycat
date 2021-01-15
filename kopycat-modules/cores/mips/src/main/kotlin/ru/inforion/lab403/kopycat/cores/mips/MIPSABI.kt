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
package ru.inforion.lab403.kopycat.cores.mips

import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class MIPSABI(core: MipsCore, bigEndian: Boolean): ABI<MipsCore>(core, 32, bigEndian) {
    @DontAutoSerialize
    override val regArguments = listOf(
            core.cpu.regs.a0.id,
            core.cpu.regs.a1.id,
            core.cpu.regs.a2.id,
            core.cpu.regs.a3.id)

    override val minimumStackAlignment = 4

    override val gprDatatype = Datatype.values().first { it.bits == this.core.cpu.regs.bits }
    override fun register(index: Int) = core.cpu.regs[index].toOperand()
    override val registerCount: Int get() = core.cpu.count()
    override val sizetDatatype = Datatype.DWORD

    override fun createContext() = MIPSContext(this)

    override val pc get() = throw NotImplementedError("PC isn't a register in MIPS")
    override val sp get() = core.cpu.regs.sp.toOperand()
    override val ra get() = core.cpu.regs.ra.toOperand()
    override val rv get() = core.cpu.regs.v0.toOperand()

    override val stackArgsOffset: Long = 0x10

    override var programCounterValue: Long
        get() = core.cpu.pc
        set(value) { core.cpu.pc = value }
}