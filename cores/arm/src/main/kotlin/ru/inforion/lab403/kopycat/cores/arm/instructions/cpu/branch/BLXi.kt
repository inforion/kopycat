package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.branch

import ru.inforion.lab403.common.extensions.cat
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.Align
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister.GPR.LR
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister.GPR.PC
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.AARMCore.InstructionSet.ARM
import ru.inforion.lab403.kopycat.modules.cores.AARMCore.InstructionSet.CURRENT

/**
 * Created by a.gladkikh on 28.01.18
 */

class BLXi(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val imm32: Immediate<AARMCore>,
           private val targetInstrSet: AARMCore.InstructionSet,
           size: Int = 4):
        AARMInstruction(cpu, Type.COND_JUMP, cond, opcode, imm32, size = size) {

    override val mnem = "BL${if (targetInstrSet != CURRENT) "X" else ""}$mcnd"

    override fun execute() {
        val pc = PC.value(core)

        if (core.cpu.CurrentInstrSet() == ARM) {
            LR.value(core, pc - 4)
        } else {
            LR.value(core, cat(pc[31..1], 1, 0))
        }

        val targetAddress = if (targetInstrSet == ARM) {
            Align(pc, 4) + imm32.value
        } else {
            pc + imm32.value
        }

        core.cpu.SelectInstrSet(targetInstrSet)
        core.cpu.BranchWritePC(targetAddress)
    }
}