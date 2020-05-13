package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.embedded.sysLink

import ru.inforion.lab403.common.extensions.bitMask
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Return from interrupt
class rfi(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "rfi"

    override fun execute() {
        PPCRegister.OEA.MSR.value(core, PPCRegister.OEA.SRR1.value(core))
        PPCRegister.UISA.PC.value(core, PPCRegister.OEA.SRR0.value(core) and bitMask(31..2))
    }
}