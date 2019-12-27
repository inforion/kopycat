package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.branch

import ru.inforion.lab403.common.extensions.cat
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister.GPR.LR
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister.GPR.PC
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.AARMCore.InstructionSet.ARM

/**
 * Created by a.gladkikh on 28.01.18
 */

class BLXr(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val rm: ARMRegister,
           size: Int): AARMInstruction(cpu, Type.VOID, cond, opcode, rm, size = size) {
    override val mnem = "BLX$mcnd"

    override fun execute() {
        val pc = PC.value(core)
        val target = rm.value(core)
        if (core.cpu.CurrentInstrSet() == ARM) {
            val nextInstrAddr = pc - 4
            LR.value(core, nextInstrAddr)
        } else {
            val nextInstrAddr = pc - 2
            LR.value(core, cat(nextInstrAddr[31..1], 1, 0))
        }
        core.cpu.BXWritePC(target)
    }
}