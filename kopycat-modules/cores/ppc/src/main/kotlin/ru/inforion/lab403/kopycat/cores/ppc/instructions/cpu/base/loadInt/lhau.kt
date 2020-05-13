package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.loadInt

import ru.inforion.lab403.common.extensions.ssext
import ru.inforion.lab403.common.extensions.usext
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Load halfword algebraic
class lhau(core: PPCCore, val condRegField: Long, val length: Boolean, val data: Long, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "lhau"

    override fun execute() {
        val ea = op2.value(core) + data.ssext(15)
        val mem = core.inw(ea).usext(15)
        op1.value(core, mem)
        op2.value(core, ea)
    }
}