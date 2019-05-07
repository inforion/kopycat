package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.BitCount
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class ThumbItDecoder(cpu: AARMCore,
                     private val constructor: (
                             cpu: AARMCore,
                             opcode: Long,
                             cond: Condition,
                             mask: Long,
                             firstCond: Long,
                             size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val mask = data[3..0]
        val firstCond = data[7..4]
        if(firstCond == 0b1111L || (firstCond == 0b1110L && BitCount(mask.asInt) != 1)) throw Unpredictable
        if(core.cpu.InITBlock()) throw Unpredictable
        return constructor(core, data, Condition.AL, mask, firstCond, 2)
    }
}