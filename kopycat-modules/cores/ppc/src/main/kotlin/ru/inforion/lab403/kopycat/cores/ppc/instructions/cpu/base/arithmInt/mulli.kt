package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.arithmInt

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.instructions.usext
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Multiply low immediate
class mulli(core: PPCCore, val condRegField: Long, val length: Boolean, val data: Long, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "mulli"

    override fun execute() {
        val extImm = data.usext(15)
        val rA = op2.value(core)
        val prod =  rA * extImm

        op1.value(core, prod[31..0])
    }
}