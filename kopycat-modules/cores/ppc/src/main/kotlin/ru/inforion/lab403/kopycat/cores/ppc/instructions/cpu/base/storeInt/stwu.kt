package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.storeInt

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.instructions.ssext
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Store word with update
class stwu(core: PPCCore, val condRegField: Long, val length: Boolean, val data: Long, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "stwu"

    override fun toString() = "$mnem $op1, ${data.ssext(15).hex8}($op2)"

    override fun execute() {
        //TODO: Displacement?
        val ea = op2.value(core) + data.ssext(15)
        core.outl(ea, op1.value(core))
        op2.value(core, ea)
    }
}