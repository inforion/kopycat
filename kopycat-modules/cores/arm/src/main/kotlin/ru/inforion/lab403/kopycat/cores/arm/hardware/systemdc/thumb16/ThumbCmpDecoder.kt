package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.SRType.SRType_LSL
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

object ThumbCmpDecoder {
    class ImmT1(cpu: AARMCore,
                val constructor: (
                        cpu: AARMCore,
                        opcode: Long,
                        cond: Condition,
                        rn: ARMRegister,
                        imm32: Immediate<AARMCore>,
                        size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val rn = GPRBank.Operand(data[10..8].asInt)
            val imm32 = Immediate<AARMCore>(data[7..0])
            return constructor(core, data, Condition.AL, rn, imm32, 2)
        }
    }
    class RegT1(cpu: AARMCore,
                val constructor: (
                        cpu: AARMCore,
                        opcode: Long,
                        cond: Condition,
                        setFlags: Boolean,
                        rd: ARMRegister,
                        rn: ARMRegister,
                        rm: ARMRegister,
                        shiftT: SRType,
                        shiftN: Int,
                        size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val rn = GPRBank.Operand(data[2..0].asInt)
            val rm = GPRBank.Operand(data[5..3].asInt)
            return constructor(core, data, Condition.AL, false, rn, rn, rm, SRType_LSL, 0, 2)
        }
    }
    class RegT2(cpu: AARMCore,
                val constructor: (
                        cpu: AARMCore,
                        opcode: Long,
                        cond: Condition,
                        setFlags: Boolean,
                        rd: ARMRegister,
                        rn: ARMRegister,
                        rm: ARMRegister,
                        shiftT: SRType,
                        shiftN: Int,
                        size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val m = data[6..3].asInt
            val n = ((data[7] shl 3) + data[2..0]).asInt
            val rn = GPRBank.Operand(n)
            val rm = GPRBank.Operand(m)
            if(n < 8 && m < 8) throw Unpredictable
            if(n == 15 || m == 15) throw Unpredictable
            return constructor(core, data, Condition.AL, false, rn, rn, rm, SRType_LSL, 0, 2)
        }
    }
}