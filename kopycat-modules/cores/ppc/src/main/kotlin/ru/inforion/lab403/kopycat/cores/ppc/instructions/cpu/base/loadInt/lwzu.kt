package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.loadInt

import ru.inforion.lab403.common.extensions.ssext
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Load word and zero with update
class lwzu(core: PPCCore, val condRegField: Long, val length: Boolean, val data: Long, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "lwzu"

    override fun execute() {
        if (((op2 as PPCRegister).reg == 0) || ((op2 as PPCRegister).reg == (op1 as PPCRegister).reg))
            throw GeneralException("Forbidden combination")

        val ea = op2.value(core) + data.ssext(15)
        val mem = core.inl(ea)
        op1.value(core, mem)
        op2.value(core, ea)
    }
}