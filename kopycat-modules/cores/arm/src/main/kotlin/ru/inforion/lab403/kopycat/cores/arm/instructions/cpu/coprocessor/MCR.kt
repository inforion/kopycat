package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.coprocessor

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.AARMv6Core

// See A8.8.98
class MCR(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            val rd: ARMRegister,
            val opcode_1: Int,
            val crn: Int,
            val cp_num:  Int,
            val opcode_2:  Int,
            val crm: Int) :
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd) {
    override val mnem = "MCR$mcnd"

    override fun execute() {
        if (!core.cpu.Coproc_Accepted(cp_num, this))
            TODO("Not implemented") //GenerateCoprocessorException()
        else
            core.cop.Coproc_SendOneWord(opcode_1, opcode_2, crn, crm, cp_num, rd.value(core))
    }
}