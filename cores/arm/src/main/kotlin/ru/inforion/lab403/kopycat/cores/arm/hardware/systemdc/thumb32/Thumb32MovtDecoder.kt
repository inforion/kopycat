package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb32

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

object Thumb32MovtDecoder { // FIXME try to merge with Thumb32MovImmDecoder
    class MOVT(
            cpu: AARMCore,
            val constructor: (
                    cpu: AARMCore,
                    opcode: Long,
                    cond: Condition,
                    rd: ARMRegister,
                    imm32: Immediate<AARMCore>
            ) -> AARMInstruction
    ) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val cond = find<Condition> { it.opcode == data[31..28].asInt } ?: Condition.AL

            val rd = ARMRegister.gpr(data[11..8].asInt)
            val imm4 = data[19..16]
            val i = data[26]
            val imm3 = data[14..12]
            val imm8 = data[7..0]

            val imm32 = Immediate<AARMCore>((imm4 shl 12) + (i shl 11) + (imm3 shl 8) + imm8)
            if (rd.reg in 15..13) throw ARMHardwareException.Unpredictable

            return constructor(core, data, cond, rd, imm32)
        }
    }
}