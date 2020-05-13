package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.coprocessor

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class MRC(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val rd: ARMRegister,
          val opcode_1: Int,
          val crn: Int,
          val cp_num:  Int,
          val opcode_2:  Int,
          val crm: Int) :
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd) {
    override val mnem = "MRC$mcnd"

    val result = ARMVariable(Datatype.WORD)
    override fun execute() {
        if (!core.cpu.Coproc_Accepted(cp_num, this))
            TODO("Not implemented") //GenerateCoprocessorException()
        else {
            val value = core.cop.Coproc_GetOneWord(opcode_1, opcode_2, crn, crm, cp_num)
            if (rd.reg != 15)
                rd.value(core, value)
            else {
                // TODO: make it faster
                core.cpu.sregs.apsr.n = value[31].toBool()
                core.cpu.sregs.apsr.z = value[30].toBool()
                core.cpu.sregs.apsr.c = value[29].toBool()
                core.cpu.sregs.apsr.v = value[28].toBool()
            }
        }

    }
}