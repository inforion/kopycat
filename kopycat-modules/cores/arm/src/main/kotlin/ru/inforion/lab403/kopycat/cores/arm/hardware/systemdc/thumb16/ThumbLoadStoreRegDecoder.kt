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

class ThumbLoadStoreRegDecoder(cpu: AARMCore,
                               private val constructor: (
                                       cpu: AARMCore,
                                       opcode: Long,
                                       cond: Condition,
                                       index: Boolean,
                                       add: Boolean,
                                       wback: Boolean,
                                       rd: ARMRegister,
                                       rn: ARMRegister,
                                       rm: ARMRegister,
                                       shiftT: SRType,
                                       shiftN: Int,
                                       size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val rt = GPRBank.Operand(data[2..0].asInt)
        val rn = GPRBank.Operand(data[5..3].asInt)
        val rm = GPRBank.Operand(data[8..6].asInt)
        val index = true
        val add = true
        val wback = false
        return constructor(core, data, Condition.AL, index, add, wback, rt, rn, rm, SRType_LSL, 0, 2)
    }
}
