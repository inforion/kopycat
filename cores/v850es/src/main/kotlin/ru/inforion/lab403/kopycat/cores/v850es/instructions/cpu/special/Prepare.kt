package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.special

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esMemory
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by r.valitov on 29.05.17.
 */

class Prepare(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "prepare"

    // Format XIII - imm, list, reg
    override fun execute() {
        val listValue = op2.value(core)
        for (i in 0..11){
            if(listValue[i] != 0L){
                val regId = when(i){
                    0 -> 30
                    1 -> 31
                    2 -> 29
                    3 -> 28
                    4 -> 23
                    5 -> 22
                    6 -> 21
                    7 -> 20
                    8 -> 27
                    9 -> 26
                    10 -> 25
                    11 -> 24
                    else -> throw GeneralException("Incorrect list index")
                }
                v850esMemory(Datatype.DWORD, core.cpu.regs.r3StackPointer - 4).value(core, v850esRegister.gpr(regId).value(core))
                core.cpu.regs.r3StackPointer = core.cpu.regs.r3StackPointer - 4
            }
        }
        core.cpu.regs.r3StackPointer = core.cpu.regs.r3StackPointer - (op1.value(core) shl 2)

        if(operands.size == 3 && (op3 is v850esRegister || op3 is Immediate))
            core.cpu.regs.r30ElementPointer = op3.value(core)
    }
}