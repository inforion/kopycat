package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.special

import ru.inforion.lab403.kopycat.cores.arm.Align
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister.GPR.PC
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by a.gladkikh on 17.01.18.
 */

class ADR(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val add: Boolean,
          val rd: ARMRegister,
          val imm: Immediate<AARMCore>,
          size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, imm, size = size) {
    override val mnem = "ADR$mcnd"

    val result = ARMVariable(Datatype.DWORD)
    override fun execute() {
        val base = Align(PC.value(core),4)
        val result = base + if (add) imm.value else -imm.value
        if (rd.reg == 15) core.cpu.ALUWritePC(result)
        else rd.value(core, result)
    }
}