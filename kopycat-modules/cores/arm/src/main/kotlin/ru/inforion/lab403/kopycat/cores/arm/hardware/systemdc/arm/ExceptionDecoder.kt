package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm

import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Undefined
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



object ExceptionDecoder {
    class Undefined(cpu: AARMCore) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction = throw Undefined
    }

    class Unpredictable(cpu: AARMCore) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction = throw Unpredictable
    }
}