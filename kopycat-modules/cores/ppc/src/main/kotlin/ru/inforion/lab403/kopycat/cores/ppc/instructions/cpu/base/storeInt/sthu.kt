package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.storeInt

import ru.inforion.lab403.common.extensions.ssext
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Store halfword with update
class sthu(core: PPCCore, val condRegField: Long, val length: Boolean, val data: Long, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "sthu"

    override fun execute() {
        //TODO: Displacement?
        val ea = op2.value(core) + data.ssext(15)
        core.outw(ea, op1.value(core)) //*Sigh* Oh, i hope you mask values by dtype...
        op2.value(core, ea)
    }
}