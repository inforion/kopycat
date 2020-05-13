package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.loadInt

import ru.inforion.lab403.common.extensions.ssext
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.enums.eUISA
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Load multiple word
/*
* WARNING: For the Server environment, the Load/Store Multiple
* instructions are not supported in Little-Endian mode. If
* they are executed in Little-Endian mode, the system
* alignment error handler is invoked.
* PowerISA V2.05, page 56
*/
class lmw(core: PPCCore, val condRegField: Long, val length: Boolean, val data: Long, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "lmw"

    override fun execute() {
        val b = if ((op2 as PPCRegister).reg == eUISA.GPR0.id)
            0L
        else
            op2.value(core)
        var ea = b + data.ssext(15)
        var r = (op1 as PPCRegister).reg
        while (r <= 31) {
            val mem = core.inl(ea)
            core.cpu.regs.gpr(r).value(core, mem)
            ++r
            ea += 4
        }
    }
}