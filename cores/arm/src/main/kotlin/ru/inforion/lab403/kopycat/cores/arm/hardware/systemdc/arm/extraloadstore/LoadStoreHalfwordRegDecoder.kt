package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.extraloadstore

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.SRType.SRType_LSL
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 16.01.18
 */

class LoadStoreHalfwordRegDecoder(
        cpu: AARMCore,
        val constructor: (
                cpu: AARMCore,
                opcode: Long,
                cond: Condition,
                index: Boolean,
                add: Boolean,
                wback: Boolean,
                rt: ARMRegister,
                rn: ARMRegister,
                rm: ARMRegister,
                shiftT: SRType,
                shiftN: Int,
                size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val cond = find<Condition> { it.opcode == data[31..28].asInt } ?: Condition.AL
        val rn = ARMRegister.gpr(data[19..16].asInt)
        val rt = ARMRegister.gpr(data[15..12].asInt)
        val rm = ARMRegister.gpr(data[3..0].asInt)

        val index = data[24] == 1L
        val add = data[23] == 1L
        val wback = data[24] == 0L || data[21] == 1L

        val shiftT = SRType_LSL
        val shiftN = 0

        if ((rt.reg == 15) || rm.reg == 15) throw Unpredictable
        if (wback && (rn.reg == 15 || rn.reg == rt.reg)) throw Unpredictable
        if (core.cpu.ArchVersion() < 6 && wback && rm.reg == rn.reg) throw Unpredictable

        return constructor(core, data, cond, index, add, wback, rt, rn, rm, shiftT, shiftN, 4)
    }
}