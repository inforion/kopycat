package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.loadInt

import ru.inforion.lab403.kopycat.cores.ppc.enums.eUISA
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Load word and zero indexed
class lwzx(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "lwzx"

    val rt = PPCRegister.gpr(fieldA)
    val ra = PPCRegister.gpr(fieldB)
    val rb = PPCRegister.gpr(fieldC)

    override fun execute() {
        val b = if ((ra as PPCRegister).reg == eUISA.GPR0.id)
            0L
        else
            ra.value(core)
        val ea = b + rb.value(core)
        val mem = core.inl(ea)
        rt.value(core, mem)
    }
}