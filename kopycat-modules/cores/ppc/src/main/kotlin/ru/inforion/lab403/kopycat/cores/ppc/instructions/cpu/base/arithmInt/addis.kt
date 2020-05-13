package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.arithmInt

import ru.inforion.lab403.common.extensions.hex4
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.enums.eUISA
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Add immediate shifted
class addis(core: PPCCore, val condRegField: Long, val length: Boolean, val data: Long, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "addis"

    override fun toString(): String = "$mnem $op1, ${if ((op2 as PPCRegister).reg == eUISA.GPR0.id) "0" else "$op2"}, ${data.hex4}(=${(data shl 16).hex8})"

    override fun execute() {
        val extImm = (data shl 16)//.ssext(31) - no need because of 32 bit system
        if ((op2 as PPCRegister).reg == eUISA.GPR0.id)
            op1.value(core, extImm)
        else
            op1.value(core, op2.value(core) + extImm)
    }
}