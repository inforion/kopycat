package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition.UN
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload.LDRL
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMImmediate
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class ThumbLoadLiteralDecoder(cpu: AARMCore) : ADecoder<AARMInstruction>(cpu) {
    private fun decodeT1(data: Long): AARMInstruction {
        val rt = GPRBank.Operand(data[10..8].asInt)
        val imm32 = ARMImmediate(data[7..0] shl 2, false)
        return LDRL(core, data, UN, true, rt, imm32, 2)
    }

    private fun decodeT2(data: Long): AARMInstruction = TODO()

    override fun decode(data: Long): AARMInstruction {
        return if (data[15..11] == 0b01001L) {
            decodeT1(data)
        } else {
            decodeT2(data)
        }
    }
}