package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.extraloadstore

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMImmediate
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 16.01.18
 */

class LoadStoreHalfwordLitDecoder(
        cpu: AARMCore,
        val constructor: (
                cpu: AARMCore,
                opcode: Long,
                cond: Condition,
                add: Boolean,
                rt: ARMRegister,
                imm32: Immediate<AARMCore>) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val cond = find<Condition> { it.opcode == data[31..28].asInt } ?: Condition.AL
        val rt = ARMRegister.gpr(data[15..12].asInt)
        val imm32 = ARMImmediate(data[11..8].shl(4) + data[3..0], true)
        val add = data[23] == 1L

        if(data[24] == data[21] || rt.reg == 15) throw Unpredictable

        return constructor(core, data, cond, add, rt, imm32)
    }
}