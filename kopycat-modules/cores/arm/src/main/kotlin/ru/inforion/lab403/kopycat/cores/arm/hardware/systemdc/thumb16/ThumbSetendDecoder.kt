package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class ThumbSetendDecoder(cpu: AARMCore,
                         private val constructor: (
                                 cpu: AARMCore,
                                 opcode: Long,
                                 cond: Condition,
                                 setBigEndian: Boolean,
                                 size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val setBigEndian = data[3] == 1L
        if(core.cpu.InITBlock()) throw Unpredictable
        return constructor(core, data, Condition.AL,  setBigEndian, 2)
    }
}