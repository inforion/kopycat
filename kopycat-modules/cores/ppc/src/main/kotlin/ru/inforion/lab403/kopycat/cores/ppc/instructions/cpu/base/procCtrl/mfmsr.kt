package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.procCtrl

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Move from machine state register
class mfmsr(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "mfmsr"

    val rt = PPCRegister.gpr(fieldA)

    override fun execute() {
        if (core.cpu.msrBits.PR)
            throw GeneralException("Privileged instruction in problem state")

        rt.value(core, core.cpu.oeaRegs.MSR)
    }
}