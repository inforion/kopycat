package ru.inforion.lab403.kopycat.cores.x86.instructions.fpu

import ru.inforion.lab403.common.extensions.ieee754
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86FprRegister
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 08.09.16.
 */

class Fld(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operand: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operand) {
    override val mnem = "fld"

    override fun execute() {
        val value = if(op2 is x86FprRegister){
            op2.value(core)
        } else {
            when(op2.dtyp){
                Datatype.DWORD -> op2.value(core).toInt().ieee754().toDouble().ieee754()
                Datatype.QWORD -> op2.value(core)
                Datatype.FPU80 -> TODO()
                else -> throw GeneralException("Incorrect datatype")
            }
        }
        (op1 as x86FprRegister).push(core, value)
    }
}