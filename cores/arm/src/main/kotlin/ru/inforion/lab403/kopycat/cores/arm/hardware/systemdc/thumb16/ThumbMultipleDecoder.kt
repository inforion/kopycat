package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegisterList
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class ThumbMultipleDecoder(cpu: AARMCore,
                           private val constructor: (
                                   cpu: AARMCore,
                                   opcode: Long,
                                   cond: Condition,
                                   wback: Boolean,
                                   rn: ARMRegister,
                                   registers: ARMRegisterList,
                                   size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val rn = ARMRegister.gpr(data[10..8].asInt)
        val wback = true
        val registerList = data[7..0]
        val registers = ARMRegisterList(core, data, registerList)
        return constructor(core, data, Condition.AL, wback, rn, registers, 2)
    }
}