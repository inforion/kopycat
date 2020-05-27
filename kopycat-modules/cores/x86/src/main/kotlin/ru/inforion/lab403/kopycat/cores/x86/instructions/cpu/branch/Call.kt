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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.CTRLR.cr0
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.cs
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.eflags
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Call(
        core: x86Core,
        operand: AOperand<x86Core>,
        opcode: ByteArray,
        prefs: Prefixes,
        val isRelative: Boolean = false,
        val isFar: Boolean = false):
        AX86Instruction(core, Type.CALL, opcode, prefs, operand) {

    override val mnem = "call"

    override fun execute() {
        if (!isFar) {
            val ip = x86Register.gpr(op1.dtyp, x86GPR.EIP)
            // order for 'offset' is important when call [esp + 0x48], see KC-752
            val offset = if (isRelative) ip.value(core) + op1.ssext(core) else op1.value(core)
            x86utils.push(core, ip.value(core), op1.dtyp, prefs)
            ip.value(core, offset)
        } else {
            val pe = cr0.pe(core)
            val vm = eflags.vm(core)

            //real-address or virtual-8086 mode
            if (!pe || (pe && vm)) {
                TODO()
            }

            //Protected mode, not virtual-8086 mode
            if (pe && !vm) {
                // order for 'offset' is important when call [esp + 0x48], see KC-752
                val ss = x86utils.getSegmentSelector(core, op1)
                val offset = op1.value(core)

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
                val ip = x86Register.gpr(prefs.opsize, x86GPR.EIP)
//                x86utils.push(cpu, Register.cs.value(cpu), Register.cs.dtyp, prefs.is16BitAddressMode)
//                x86utils.push(cpu, regip.value(cpu), regip.dtyp, prefs.is16BitAddressMode)
                x86utils.push(core, cs.value(core), prefs.opsize, prefs)
                x86utils.push(core, ip.value(core), prefs.opsize, prefs)

                cs.value(core, ss)
                ip.value(core, offset)
            }
        }
    }
}