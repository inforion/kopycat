package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMImmediate
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class ThumbSystemDecoder(cpu: AARMCore,
                         private val constructor: (
                                 cpu: AARMCore,
                                 opcode: Long,
                                 cond: Condition,
                                 imm: Immediate<AARMCore>,
                                 size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val imm = ARMImmediate(data[7..0], true)
        return constructor(core, data, Condition.AL, imm, 2)
    }
}