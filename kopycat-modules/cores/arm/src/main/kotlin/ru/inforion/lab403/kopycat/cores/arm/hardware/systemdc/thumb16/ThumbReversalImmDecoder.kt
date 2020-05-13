package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class ThumbReversalImmDecoder(cpu: AARMCore,
                              private val constructor: (
                                   cpu: AARMCore,
                                   opcode: Long,
                                   cond: Condition,
                                   setflag: Boolean,
                                   rd: ARMRegister,
                                   rm: ARMRegister,
                                   imm: Immediate<AARMCore>,
                                   size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val rd = GPRBank.Operand(data[2..0].asInt)
        val rm = GPRBank.Operand(data[5..3].asInt)
        return constructor(core, data, Condition.AL, !core.cpu.InITBlock(), rd, rm, Immediate(0),2)
    }
}