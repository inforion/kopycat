package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class ThumbCpsDecoder (cpu: AARMCore,
                       private val constructor: (
                               cpu: AARMCore,
                               opcode: Long,
                               cond: Condition,
                               im: Boolean,
                               size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        if(data[2..0] == 0L) throw Unpredictable
        val im = data[4]
        if(core.cpu.InITBlock()) throw Unpredictable
        return constructor(core, data, Condition.AL, im == 1L, 2)
    }
}