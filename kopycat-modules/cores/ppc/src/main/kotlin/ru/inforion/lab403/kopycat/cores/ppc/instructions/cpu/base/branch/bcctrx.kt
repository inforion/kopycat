package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.branch

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Branch conditional to count register
class bcctrx(core: PPCCore, val options: Int, val condition: Int, val fieldC: Int, val linkage: Boolean):
        APPCInstruction(core, Type.COND_CALL) {
    override val mnem = "bcctr${if (linkage) "l" else ""}"

    //Now BH is not used

    override fun execute() {
        if (linkage)
            core.cpu.regs.LR = core.cpu.regs.PC // + 4 // PC already incremented

        val cond_ok = options[4].toBool() or (core.cpu.crBits.bit(condition) == options[3].toBool())
        if (cond_ok)
            core.cpu.regs.PC = core.cpu.regs.CTR and 0xFFFF_FFFC //cut off 2 lsb
    }
}