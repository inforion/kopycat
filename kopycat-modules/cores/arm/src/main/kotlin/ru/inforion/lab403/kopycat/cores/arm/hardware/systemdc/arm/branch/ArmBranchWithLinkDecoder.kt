package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.branch

import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition.UN
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.branch.BLXi
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMImmediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

object ArmBranchWithLinkDecoder {
    class A1(cpu: AARMCore) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val imm24 = data[23..0]
            val imm26 = imm24 shl 2
            val targetInstrSet = AARMCore.InstructionSet.ARM
            val imm32 = ARMImmediate(signext(imm26, 26).asLong, true)
            return BLXi(core, data, UN, imm32, targetInstrSet, 4)
        }
    }

    class A2(cpu: AARMCore) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val hbit = data[24]
            val imm24 = data[23..0]
            val imm26 = (imm24 shl 2) or (hbit shl 1)
            val targetInstrSet = AARMCore.InstructionSet.THUMB
            val imm32 = ARMImmediate(signext(imm26, 26).asLong, true)
            return BLXi(core, data, UN, imm32, targetInstrSet, 4)
        }
    }
}