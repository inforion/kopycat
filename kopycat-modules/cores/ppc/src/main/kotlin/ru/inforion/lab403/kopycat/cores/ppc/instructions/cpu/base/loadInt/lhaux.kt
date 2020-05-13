package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.loadInt

import ru.inforion.lab403.common.extensions.usext
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Load halfword algebraic with update indexed
class lhaux(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "lhaux"

    val rt = PPCRegister.gpr(fieldA)
    val ra = PPCRegister.gpr(fieldB)
    val rb = PPCRegister.gpr(fieldC)

    override fun execute() {
        if (((ra as PPCRegister).reg == 0) || ((ra as PPCRegister).reg == (rt as PPCRegister).reg))
            throw GeneralException("Forbidden combination")

        val ea = ra.value(core) + rb.value(core)
        val mem = core.inw(ea).usext(15)
        rt.value(core, mem)
        ra.value(core, ea)
    }
}