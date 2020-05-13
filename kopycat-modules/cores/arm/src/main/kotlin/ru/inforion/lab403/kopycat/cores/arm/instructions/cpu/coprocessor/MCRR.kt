package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.coprocessor

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



// See A8.8.99
class MCRR(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val cp: Int,
           val opc1: Int,
           val rt: ARMRegister,
           val rt2: ARMRegister,
           val crm: Int) : AARMInstruction(cpu, Type.VOID, cond, opcode, rt, rt2) {

    override val mnem = "MCRR$mcnd"

    val result = ARMVariable(Datatype.WORD)
    override fun execute() {
        if (!core.cpu.Coproc_Accepted(cp, this))
            TODO("Not implemented") //GenerateCoprocessorException()
        else
            core.cop.Coproc_SendTwoWords(rt2.value(core), rt.value(core), cp, opc1, crm)
    }
}