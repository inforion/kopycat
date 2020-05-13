package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

object ThumbArithmImmDecoder {
    class T1(cpu: AARMCore,
             private val constructor: (
                     cpu: AARMCore,
                     opcode: Long,
                     cond: Condition,
                     setFlags: Boolean,
                     rd: ARMRegister,
                     rn: ARMRegister,
                     imm32: Immediate<AARMCore>,
                     size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val rd = GPRBank.Operand(data[2..0].asInt)
            val rn = GPRBank.Operand(data[5..3].asInt)
            val imm3 = Immediate<AARMCore>(data[8..6])
            val setFlag = !core.cpu.InITBlock()
            return constructor(core, data, Condition.AL, setFlag, rd, rn, imm3, 2)
        }
    }

    class T2(cpu: AARMCore,
             private val constructor: (
                     cpu: AARMCore,
                     opcode: Long,
                     cond: Condition,
                     setFlags: Boolean,
                     rd: ARMRegister,
                     rn: ARMRegister,
                     imm32: Immediate<AARMCore>,
                     size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val rd = GPRBank.Operand(data[10..8].asInt)
            val rn = GPRBank.Operand(data[10..8].asInt)
            val imm8 = Immediate<AARMCore>(data[7..0])
            val setFlag = !core.cpu.InITBlock()
            return constructor(core, data, Condition.AL, setFlag, rd, rn, imm8, 2)
        }
    }
}