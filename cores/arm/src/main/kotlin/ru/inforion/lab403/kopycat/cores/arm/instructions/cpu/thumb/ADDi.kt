package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.thumb

import ru.inforion.lab403.kopycat.cores.arm.AddWithCarry
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 18.01.18
 */

class ADDi(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           private val setFlags: Boolean,
           val rd: ARMRegister,
           val rn: ARMRegister,
           val imm32: Immediate<AARMCore>,
           size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rn, imm32, size = size) {

    override val mnem = "ADD${if(setFlags) "S" else ""}$mcnd"

    override fun execute() {
        val (result, carry, overflow) = AddWithCarry(rn.dtyp.bits, rn.value(core), imm32.value, 0)
        rd.value(core, result)
        if (setFlags)
            FlagProcessor.processArithmFlag(core, result, carry, overflow)
    }
}
