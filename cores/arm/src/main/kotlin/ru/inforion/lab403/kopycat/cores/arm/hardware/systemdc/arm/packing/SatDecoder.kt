package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.packing

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.DecodeImmShift
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.UInt
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class SatDecoder(cpu: AARMCore,
                 val constructor: (
                         cpu: AARMCore,
                         opcode: Long,
                         cond: Condition,
                         rd: ARMRegister,
                         shiftT: SRType,
                         shiftN: Long,
                         saturateTo: Immediate<AARMCore>,
                         rn: ARMRegister) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val cond = find<Condition> { it.opcode == data[31..28].asInt } ?: Condition.AL
        val rn = ARMRegister.gpr(data[3..0].asInt)
        val rd = ARMRegister.gpr(data[15..12].asInt)
        val sh = data[6]
        val saturateTo = Immediate<AARMCore>(UInt(data[20..16], 32) + 1)
        val imm5 = data[11..7]
        val (shiftT, shiftN) = DecodeImmShift(sh.shl(1), imm5)

        if(rd.reg == 15 || rn.reg == 15) throw Unpredictable

        return constructor(core, data, cond, rd, shiftT, shiftN, saturateTo, rn)
    }
}