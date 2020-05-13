package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.sysLink

import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



class sc(core: PPCCore, val lev: Long):
        APPCInstruction(core, Type.CALL) {
    override val mnem = "sc"

    override fun execute() {
        core.cpu.oeaRegs.SRR0 = core.cpu.regs.PC + 4
        core.cpu.oeaRegs.SRR1 = core.cpu.oeaRegs.MSR and 0x87C0FFFF //1-3, 10-15 bits are zero

        TODO("Throw sc exception")
        TODO("Alter MSR")
        TODO("PC.offset = 0xC00")
    }
}