package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.branch

import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.branch.B
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMImmediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

object ArmBranchDecoder {
    class A1(cpu: AARMCore) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val imm24 = data[23..0]
            val imm26 = imm24 shl 2
            val imm32 = ARMImmediate(signext(imm26, 26).asLong, true)
            return B(core, data, Condition.UN, imm32)
//            TODO("WHEN CACHE FOR THUMB ENABLE THIS SHOULD BE FIXED!")
        }
    }
}