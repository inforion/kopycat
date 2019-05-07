package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.extraloadstore

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

class LoadStoreDualRegDecoder(
        cpu: AARMCore,
        val constructor: (
                cpu: AARMCore,
                opcode: Long,
                cond: Condition,
                index: Boolean,
                add: Boolean,
                wback: Boolean,
                rn: ARMRegister,
                rt: ARMRegister,
                rt2: ARMRegister,
                rm: ARMRegister) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val cond = find<Condition> { it.opcode == data[31..28].asInt } ?: Condition.AL
        val rn = ARMRegister.gpr(data[19..16].asInt)
        val rt = ARMRegister.gpr(data[15..12].asInt)
        val rt2 = ARMRegister.gpr(data[15..12].asInt + 1)
        val rm = ARMRegister.gpr(data[3..0].asInt)

        val index = data[24] == 1L
        val add = data[23] == 1L
        val wback = data[24] == 0L || data[21] == 1L

        if(data[24] == 0L && data[21] == 1L) throw Unpredictable
        if ((rt2.reg == 15) || rm.reg == 15) throw Unpredictable
        if (wback && (rn.reg == 15 || rn.reg == rt2.reg)) throw Unpredictable
        if (core.cpu.ArchVersion() < 6 && wback && rm.reg == rn.reg) throw Unpredictable

        return constructor(core, data, cond, index, add, wback, rn, rt, rt2, rm)
    }
}