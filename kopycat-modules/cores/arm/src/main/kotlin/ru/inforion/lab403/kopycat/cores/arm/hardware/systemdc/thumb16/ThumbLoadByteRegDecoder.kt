package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.SRType.SRType_LSL
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class ThumbLoadByteRegDecoder (cpu: AARMCore,
                               private val constructor: (
                                       cpu: AARMCore,
                                       opcode: Long,
                                       cond: Condition,
                                       index: Boolean,
                                       add: Boolean,
                                       wback: Boolean,
                                       rn: ARMRegister,
                                       rm: ARMRegister,
                                       rt: ARMRegister,
                                       shiftN: Int,
                                       shiftT: SRType,
                                       size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val rt = GPRBank.Operand(data[2..0].asInt)
        val rn = GPRBank.Operand(data[5..3].asInt)
        val rm = GPRBank.Operand(data[8..6].asInt)
        return constructor(core, data, Condition.AL, true, true, false, rn, rm, rt, 0, SRType_LSL, 2)
    }
}