package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.media

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by the bat on 01.02.18
 */

class MOVT(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val rd: ARMRegister,
           val imm: Immediate<AARMCore>):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, imm) {

    override val mnem = "MOVT$mcnd"

    override fun execute() = rd.bits(core, 31..16, imm.value)
}