package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.special

import ru.inforion.lab403.kopycat.cores.arm.Align
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class ADR(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val add: Boolean,
          val rd: ARegister<AARMCore>,
          val imm: Immediate<AARMCore>,
          size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, imm, size = size) {
    override val mnem = "ADR$mcnd"

    val result = ARMVariable(Datatype.DWORD)
    override fun execute() {
        val base = Align(core.cpu.pc,4)
        val result = base + if (add) imm.value else -imm.value
        if (rd.reg == core.cpu.regs.pc.reg) core.cpu.ALUWritePC(result)
        else rd.value(core, result)
    }
}