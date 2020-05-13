package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.ldstReverse

import ru.inforion.lab403.common.extensions.swap16
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.ppc.enums.eUISA
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Store halfword byte-reverse indexed
class sthbrx(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "sthbrx"

    val rs = PPCRegister.gpr(fieldA)
    val ra = PPCRegister.gpr(fieldB)
    val rb = PPCRegister.gpr(fieldC)

    override fun execute() {
        val b = if ((ra as PPCRegister).reg == eUISA.GPR0.id)
            0L
        else
            ra.value(core)
        val ea = b + rb.value(core)
        core.outw(ea, rs.value(core).swap16()) //*Sigh* Oh, i hope you mask values by dtype...
    }
}