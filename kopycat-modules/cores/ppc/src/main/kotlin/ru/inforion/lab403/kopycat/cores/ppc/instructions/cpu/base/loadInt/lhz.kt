package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.loadInt

import ru.inforion.lab403.common.extensions.ssext
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.enums.eUISA
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Load halfword and zero
class lhz(core: PPCCore, val condRegField: Long, val length: Boolean, val data: Long, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "lhz"

    override fun execute() {
        val b = if ((op2 as PPCRegister).reg == eUISA.GPR0.id)
            0L
        else
            op2.value(core)
        val ea = b + data.ssext(15)
        val mem = core.inw(ea)
        op1.value(core, mem)
    }
}