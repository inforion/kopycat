package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.arithmInt

import ru.inforion.lab403.common.extensions.hex16
import ru.inforion.lab403.common.extensions.hex4
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.extensions.mask
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.enums.eUISA
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.instructions.ssext
import ru.inforion.lab403.kopycat.cores.ppc.instructions.usext
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



// Add immediate
class addi(core: PPCCore, val condRegField: Long, val length: Boolean, val data: Long, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "addi"

    override fun toString(): String = "$mnem $op1, ${if ((op2 as PPCRegister).reg == eUISA.GPR0.id) "0" else "$op2"}, ${data.hex4}(=${data.ssext(15).hex8})"

    override fun execute() {
        val extImm = data.usext(15) // Carry fix
        if ((op2 as PPCRegister).reg == eUISA.GPR0.id)
            op1.value(core, extImm)
        else
            op1.value(core, op2.value(core) + extImm)
    }
}