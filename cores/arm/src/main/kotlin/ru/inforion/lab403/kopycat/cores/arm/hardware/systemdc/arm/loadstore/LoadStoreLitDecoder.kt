package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.loadstore

import ru.inforion.lab403.common.extensions.*
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

class LoadStoreLitDecoder(
        cpu: AARMCore,
        private val checkRt: Boolean,
        val constructor: (
                cpu: AARMCore,
                opcode: Long,
                cond: Condition,
                add: Boolean,
                rt: ARMRegister,
                imm: Immediate<AARMCore>,
                size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val cond = find<Condition> { it.opcode == data[31..28].asInt }?: Condition.AL
        val rt = ARMRegister.gpr(data[15..12].asInt)
        val add = data[23] == 1L
        val imm32 = ARMImmediate(signext(data[11..0], 12).asLong, true)

        if(checkRt && rt.reg == 15) throw Unpredictable
        return constructor(core, data, cond, add, rt, imm32, 4)
    }
}