package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.memSync

import ru.inforion.lab403.kopycat.cores.ppc.enums.eUISA
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Load word and reserve indexed
class lwarx(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "lwarx"

    val rt = PPCRegister.gpr(fieldA)
    val ra = PPCRegister.gpr(fieldB)
    val rb = PPCRegister.gpr(fieldC)

    override fun execute() {
        val b = if ((ra as PPCRegister).reg == eUISA.GPR0.id)
            0L
        else
            ra.value(core)
        val ea = b + rb.value(core)
        TODO("I have no ducking idea, how this thing have to work")
        /*core.cpu.regs.RESERVE = 1
        core.cpu.regs.RESERVE_ADDR = 1
        val mem = core.bus.read(Datatype.DWORD, ea)
        rt.value(core, mem)*/
    }
}