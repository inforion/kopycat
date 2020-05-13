package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.storeInt

import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Store byte with update indexed
class stbux(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "stbux"

    val rs = PPCRegister.gpr(fieldA)
    val ra = PPCRegister.gpr(fieldB)
    val rb = PPCRegister.gpr(fieldC)

    override fun execute() {
        val ea = ra.value(core) + rb.value(core)
        core.outb(ea, rs.value(core)) //*Sigh* Oh, i hope you mask values by dtype...
        ra.value(core, ea)
    }
}