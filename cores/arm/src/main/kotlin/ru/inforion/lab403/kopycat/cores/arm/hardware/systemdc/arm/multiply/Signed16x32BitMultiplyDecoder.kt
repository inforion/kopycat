package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.multiply

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 16.01.18
 */

class Signed16x32BitMultiplyDecoder(
        cpu: AARMCore,
        val constructor: (
                cpu: AARMCore,
                opcode: Long,
                cond: Condition,
                rd: ARMRegister,
                ra: ARMRegister,
                rm: ARMRegister,
                rn: ARMRegister,
                mHigh: Boolean) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val cond = find<Condition> { it.opcode == data[31..28].asInt }?: Condition.AL
        val rd = ARMRegister.gpr(data[19..16].asInt)
        val ra = ARMRegister.gpr(data[15..12].asInt)
        val rm = ARMRegister.gpr(data[11..8].asInt)
        val rn = ARMRegister.gpr(data[3..0].asInt)

        val mHigh = data[6] == 1L

        if(rd.reg == 15 || rn.reg == 15 || rm.reg == 15 || ra.reg == 15) throw Unpredictable

        return constructor(core, data, cond, rd, ra, rm, rn, mHigh)
    }
}