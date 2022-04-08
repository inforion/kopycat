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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.branch

import ru.inforion.lab403.common.extensions.clr
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU.Companion.LME
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Displacement
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Far
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Phrase
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Jmp(
        core: x86Core,
        operand: AOperand<x86Core>,
        opcode: ByteArray,
        prefs: Prefixes,
        val isRelative: Boolean = false,
        val isFar: Boolean = false):
        AX86Instruction(core, Type.JUMP, opcode, prefs, operand) {
    override val mnem = "jmp"

    override fun execute() {
        if (!isFar) {
            val regip = core.cpu.regs.gpr(x86GPR.RIP, prefs.opsize)
            val eip = if (isRelative) {
                val offset = op1.usext(core)
                val eip = regip.value
                eip + offset
            } else op1.value(core)
            if (!x86utils.isWithinCodeSegmentLimits(eip))
                throw x86HardwareException.GeneralProtectionFault(core.pc, core.cpu.sregs.cs.value)

            regip.value = eip
        } else {
            val pe = core.cpu.cregs.cr0.pe
            val vm = core.cpu.flags.vm

            //real-address or virtual-8086 mode
            if (!pe || (pe && vm)) {
                core.cpu.regs.gpr(x86GPR.RIP, prefs.opsize).value = op1.value(core)
                core.cpu.sregs.cs.value = (op1 as x86Far).ss
            }

            //Protected mode, not virtual-8086 mode
            if (pe && !vm) {
                val ss = when (op1) {
                    is x86Far -> (op1 as x86Far).ss
                    is x86Displacement -> (op1 as x86Displacement).ssr.value(core)
                    is x86Phrase -> (op1 as x86Phrase).ssr.value(core)
                    else -> throw GeneralException("Incorrect operand type")
                }

//                val ss = (op1 as Far).ss
                val address = op1.value(core)
                core.cpu.sregs.cs.value = ss
                val regip = core.cpu.regs.gpr(x86GPR.RIP, prefs.opsize)
                regip.value = address
//                SegmentsToCheck[] = {CS, DS, ES, FS, GS, SS};
//                if(!CheckEffectiveAddresses(SegmentsToCheck) || TargetOperand.SegmentSelector == 0) Exception(GP(0)); //effective address in the CS, DS, ES, FS, GS, or SS segment is illegal
//                if(!IsWithinDescriptorTableLimits(SegmentSelector.Index)) Exception(GP(NewSelector));
//                val gdt = dev.mmu.readSegmentDescriptorData(ss)
//                when (type) {
//                    TypeConformingCodeSegment -> TODO()
//                    TypeNonConformingCodeSegment -> TODO()
//                    TypeCallGate -> TODO()
//                    TypeTaskGate -> TODO()
//                    TypeTaskStateSegment -> TODO()
//                    else -> TODO()
//                }
//                Register.cs.value(cpu, ss)
//                val regip = Register.gpr(prefs.opsize, GPR.EIP.id)
//                val address = op1.value(cpu)
//                regip.value(cpu, address)
            }
        }
    }
}