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
package ru.inforion.lab403.kopycat.cores.x86.hardware.processors

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACPU
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.registers.*
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.x86SystemDecoder
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.control.Nop
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.modules.BUS32
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.serializer.loadValue
import ru.inforion.lab403.kopycat.serializer.storeValues


class x86CPU(val x86: x86Core, name: String):
    ACPU<x86CPU, x86Core, AX86Instruction, x86GPR>(x86, name), IAutoSerializable {

    companion object {
        const val EFER = 0xC0000080uL
        const val LME = 8 // Long Mode Enable
    }

    enum class Mode { R16, R32, R64 }

    // Cached CS.D value
    var csd = false
    // Cached CS.L value
    var csl = false

    // Operand and Address size mode! Not real or protected!
    // Real or protected mode defined by cregs.vpe
    val mode: Mode get() = when {
        csl && cregs.cr4.pae && x86.config.efer[LME].truth && cregs.cr0.pg -> Mode.R64
        csd && cregs.cr0.pe -> Mode.R32
        else -> Mode.R16
    }

    /** Long mode: compatibility submode AKA IA-32e */
    val is64BitCompatibilityMode get() = mode != Mode.R64 && x86.config.efer[LME].truth && x86.cpu.cregs.cr0.pg

    override fun reg(index: Int): ULong = regs[index].value
    override fun reg(index: Int, value: ULong) = run { regs[index].value = value }
    override fun count() = regs.count()
    override fun flags() = flags.eflags.value

    /**
     * Stub for debugging purpose of x86CPU core
     * Read - returns physical CPU.PC address
     * Write - setup only offset of virtual address within cs segment
     */
    override var pc: ULong
        get() = regs.rip.value
        set(value) = run { regs.rip.value = value }

    val regs = GPRBank()
    val flags = FLBank(x86) // TODO: split to flags and eflags
    val sregs = SSRBank(x86)
    val dregs = DBGBank()
    val cregs = CTRLBank(x86)

    private val decoder = x86SystemDecoder(x86, this)

    override fun reset() {
        super.reset()
        decoder.reset()
        regs.reset()
        flags.reset()
        sregs.reset()
        insn = Nop.create(x86)
    }

    override fun decode() {
        insn = decoder.decode(regs.rip.value)
//        log.info { "cs=${sregs.cs.value.hex} eip=${regs.eip.value.hex} $insn" }
    }

    override fun execute(): Int {
        regs.rip.value += insn.size.uint
//        log.info { insn }
        insn.execute()

        if (x86.cop.intShadow != 0) {
            x86.cop.intShadow -= 1
        }

        return 1  // TODO: get from insn.execute()
    }

    override fun stringify() = buildString {
        val where = x86.mmu.translate(regs.rip.value, sregs.cs.id, 1, AccessAction.LOAD)
        appendLine("x86 CPU: PC = 0x${where.hex8}")
        appendLine(regs.stringify())
        append(sregs.stringify())
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super<IAutoSerializable>.serialize(ctxt)
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super<IAutoSerializable>.deserialize(ctxt, snapshot)
    }
}
