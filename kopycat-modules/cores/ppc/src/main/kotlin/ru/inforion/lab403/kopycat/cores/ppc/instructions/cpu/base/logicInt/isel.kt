package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.logicInt

import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Integer select
class isel(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val fieldD: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "isel"

    val rt = PPCRegister.gpr(fieldA)
    val ra = PPCRegister.gpr(fieldB)
    val rb = PPCRegister.gpr(fieldC)


    override fun execute() {
        val a = if (fieldB == 0) 0L else ra.value(core)
        val value = if (core.cpu.crBits.bit(fieldD))
            a
        else
            rb.value(core)
        rt.value(core, value)
    }
}