package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMImmediate
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMImmediateShift
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



object PopPushDecoder {
    class A1(cpu: AARMCore,
             private val checkRt: Boolean,
             val constructor: (
                     cpu: AARMCore,
                     opcode: Long,
                     cond: Condition,
                     postindex: Boolean,
                     add: Boolean,
                     rn: ARMRegister,
                     rt: ARMRegister,
                     imm: AOperand<AARMCore>) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val cond = find<Condition> { it.opcode == data[31..28].asInt } ?: Condition.AL
            val rt = GPRBank.Operand(data[15..12].asInt)
            val rn = GPRBank.Operand(data[19..16].asInt)
            val postindex = true
            val add = data[23] == 1L
            val imm32 = ARMImmediate(data[11..0], true)

            val pc = core.cpu.regs.pc.reg

            if((checkRt && rt.reg == pc) || rn.reg == pc || rn.reg == rt.reg) throw Unpredictable
            return constructor(core, data, cond, postindex, add, rn, rt, imm32)
        }
    }

    class A2(cpu: AARMCore,
             private val checkRt: Boolean,
             val constructor: (
                     cpu: AARMCore,
                     opcode: Long,
                     cond: Condition,
                     postindex: Boolean,
                     add: Boolean,
                     rn: ARMRegister,
                     rt: ARMRegister,
                     imm: AOperand<AARMCore>) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val cond = find<Condition> { it.opcode == data[31..28].asInt } ?: Condition.AL
            val rt = GPRBank.Operand(data[15..12].asInt)
            val rn = GPRBank.Operand(data[19..16].asInt)
            val m = data[3..0].asInt
            val postindex = true
            val add = data[23] == 1L

            val pc = core.cpu.regs.pc.reg

            if ((checkRt && rt.reg == pc) || rn.reg == pc || rn.reg == rt.reg || m == 15) throw Unpredictable

            return constructor(core, data, cond, postindex, add, rn, rt, ARMImmediateShift(core, data))
        }
    }
}