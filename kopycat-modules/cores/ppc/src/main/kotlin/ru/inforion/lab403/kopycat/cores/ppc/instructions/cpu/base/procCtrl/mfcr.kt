package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.procCtrl

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Move from condition register
class mfcr(core: PPCCore, val field: Int, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "mfcr"

    override fun execute() {
        op1.value(core, core.cpu.regs.CR)
    }
}