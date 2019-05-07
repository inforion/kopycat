package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.packing

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.UInt
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class Sat16Decoder(cpu: AARMCore,
                   val constructor: (
                           cpu: AARMCore,
                           opcode: Long,
                           cond: Condition,
                           rd: ARMRegister,
                           saturateTo: Immediate<AARMCore>,
                           rn: ARMRegister) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val cond = find<Condition> { it.opcode == data[31..28].asInt } ?: Condition.AL
        val rn = ARMRegister.gpr(data[3..0].asInt)
        val rd = ARMRegister.gpr(data[15..12].asInt)
        val satImm = data[20..16]
        val saturateTo = Immediate<AARMCore>(UInt(satImm, 32))

        if(rd.reg == 15 || rn.reg == 15) throw Unpredictable

        return constructor(core, data, cond, rd, saturateTo, rn)
    }
}