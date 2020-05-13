package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.special

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class SETEND(cpu: AARMCore,
             opcode: Long,
             cond: Condition,
             private val setBigEnd: Boolean,
             size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, size = size) {
    override val mnem = "SETEND${if(setBigEnd)"BE" else "LE"}"

    val result = ARMVariable(Datatype.DWORD)
    override fun execute() {
        core.cpu.status.ENDIANSTATE = setBigEnd
    }
}