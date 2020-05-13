package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.storeInt

import ru.inforion.lab403.common.extensions.ssext
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Store byte with update
class stbu(core: PPCCore, val condRegField: Long, val length: Boolean, val data: Long, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "stbu"

    override fun execute() {
        //TODO: Displacement?
        val ea = op2.value(core) + data.ssext(15)
        core.outb(ea, op1.value(core)) //*Sigh* Oh, i hope you mask values by dtype...
        op2.value(core, ea)
    }
}