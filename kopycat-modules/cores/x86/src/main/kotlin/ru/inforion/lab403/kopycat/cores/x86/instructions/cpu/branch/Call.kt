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

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
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
            val ip = core.cpu.regs.gpr(x86GPR.RIP, op1.dtyp).toOperand()
            // order for 'offset' is important when call [esp + 0x48], see KC-752
            val offset = if (isRelative) ip.value(core) + op1.usext(core) else op1.value(core)
            x86utils.push(core, ip.value(core), op1.dtyp, prefs)
            ip.value(core, offset)
        } else {
            val pe = core.cpu.cregs.cr0.pe
            val vm = core.cpu.flags.vm

            //real-address or virtual-8086 mode
            if (!pe || (pe && vm)) {
                val cs = core.cpu.sregs.cs
                val ip = core.cpu.regs.gpr(x86GPR.RIP, prefs.opsize)

                x86utils.push(core, cs.value, prefs.opsize, prefs)
                x86utils.push(core, ip.value, prefs.opsize, prefs)

                val ss = x86utils.getSegmentSelector(core, op1)
                val offset = op1.value(core)

                cs.value = ss
                ip.value = offset
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

                val ip = core.cpu.regs.gpr(x86GPR.RIP, prefs.opsize)
                x86utils.push(core, core.cpu.sregs.cs.value, prefs.opsize, prefs)
                x86utils.push(core, ip.value, prefs.opsize, prefs)

                core.cpu.sregs.cs.value = ss
                ip.value = offset
            }
        }
    }
}