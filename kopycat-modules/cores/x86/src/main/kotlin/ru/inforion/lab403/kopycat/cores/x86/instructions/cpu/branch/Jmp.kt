package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Displacement
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Far
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Phrase
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.CTRLR.cr0
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.cs
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.eflags
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
            val regip = x86Register.gpr(prefs.opsize, x86GPR.EIP.id)
            val eip = if (isRelative) {
                val offset = op1.ssext(core)
                val eip = regip.value(core)
                eip + offset
            } else op1.value(core)
            if (!x86utils.isWithinCodeSegmentLimits(eip))
                throw x86HardwareException.GeneralProtectionFault(core.pc, cs.value(core))

            regip.value(core, eip)
        } else {
            val pe = cr0.pe(core)
            val vm = eflags.vm(core)

            //real-address or virtual-8086 mode
            if (!pe || (pe && vm)) {
                x86Register.gpr(prefs.opsize, x86GPR.EIP.id).value(core, op1.value(core))
                cs.value(core, (op1 as x86Far).ss)
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
                cs.value(core, ss)
                val regip = x86Register.gpr(prefs.opsize, x86GPR.EIP.id)
                regip.value(core, address)
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