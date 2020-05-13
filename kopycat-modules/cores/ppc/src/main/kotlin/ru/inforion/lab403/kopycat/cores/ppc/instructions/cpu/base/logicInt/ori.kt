package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.logicInt

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//OR immediate
class ori(core: PPCCore, val condRegField: Long, val length: Boolean, val data: Long, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "ori"

    override fun toString(): String = "$mnem $op2, $op1, ${data.hex8}"

    override fun execute() {
        op2.value(core, op1.value(core) or data)
    }
}