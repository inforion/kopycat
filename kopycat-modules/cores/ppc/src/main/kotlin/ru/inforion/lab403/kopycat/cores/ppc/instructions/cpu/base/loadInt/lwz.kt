package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.loadInt

import ru.inforion.lab403.common.extensions.hex4
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.enums.eUISA
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.instructions.ssext
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Load word and zero
class lwz(core: PPCCore, val condRegField: Long, val length: Boolean, val data: Long, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "lwz"

    override fun toString() = "$mnem $op1, ${if ((op2 as PPCRegister).reg == eUISA.GPR0.id) "0" else "$op2"}, ${data.hex4}(=${data.ssext(15).hex8})"

    override fun execute() {
        val b = if ((op2 as PPCRegister).reg == eUISA.GPR0.id)
            0L
        else
            op2.value(core)
        val ea = b + data.ssext(15)
        val mem = core.inl(ea)
        op1.value(core, mem)
    }
}