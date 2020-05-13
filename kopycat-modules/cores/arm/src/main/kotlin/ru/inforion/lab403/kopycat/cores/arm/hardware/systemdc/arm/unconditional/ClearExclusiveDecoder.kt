package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.unconditional

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.unconditional.CLREX
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class ClearExclusiveDecoder(cpu: AARMCore) : ADecoder<AARMInstruction>(cpu) {

    // Encoding A1
    override fun decode(data: Long): AARMInstruction {
        return CLREX(core, data, Condition.AL)
    }
}