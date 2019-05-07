package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

object ThumbSPImmDecoder {
    class T1(cpu: AARMCore,
             val constructor: (
                     cpu: AARMCore,
                     opcode: Long,
                     cond: Condition,
                     setFlags: Boolean,
                     rn: ARMRegister,
                     rd: ARMRegister,
                     imm32: Immediate<AARMCore>,
                     size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val setFlags = false
            val rd = ARMRegister.gpr(data[10..8].asInt)
            val imm32 = Immediate<AARMCore>(data[7..0] shl 2)
            return constructor(core, data, Condition.AL, setFlags, rd, ARMRegister.gpr(13), imm32, 2)
        }
    }
    class T2(cpu: AARMCore,
             val constructor: (
                     cpu: AARMCore,
                     opcode: Long,
                     cond: Condition,
                     setFlags: Boolean,
                     rn: ARMRegister,
                     rd: ARMRegister,
                     imm32: Immediate<AARMCore>,
                     size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val setFlags = false
            val rd = ARMRegister.gpr(13)
            val imm32 = Immediate<AARMCore>(data[6..0] shl 2)
            return constructor(core, data, Condition.AL, setFlags, rd, rd, imm32, 2)
        }
    }
}
