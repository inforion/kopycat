package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.thumb

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class CBZ(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val rn: ARMRegister,
          private val nonZero: Boolean,
          val imm: Immediate<AARMCore>,
          size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, imm, size = size) {

    override val mnem = "CBZ${if(nonZero)"N" else ""}"

    override fun execute() {
        if(nonZero xor (rn.value(core) == 0L))
            core.cpu.BranchWritePC(core.cpu.pc + imm.value)
    }
}
