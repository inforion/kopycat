package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.ldstReverse

import ru.inforion.lab403.common.extensions.swap32
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.ppc.enums.eUISA
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Store word byte-reverse indexed
class stwbrx(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "stwbrx"

    val rs = PPCRegister.gpr(fieldA)
    val ra = PPCRegister.gpr(fieldB)
    val rb = PPCRegister.gpr(fieldC)

    override fun execute() {
        val b = if ((ra as PPCRegister).reg == eUISA.GPR0.id)
            0L
        else
            ra.value(core)
        val ea = b + rb.value(core)
        core.outl(ea, rs.value(core).swap32()) //*Sigh* Oh, i hope you mask values by dtype...
    }
}