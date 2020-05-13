package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.branch

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Branch conditional
class bcx(core: PPCCore, val options: Long, val condition: Int, val address: Long, val absolute: Boolean, val linkage: Boolean):
        APPCInstruction(core, Type.COND_CALL) {
    override val mnem = "bc${if (linkage) "l" else ""}${if (absolute) "a" else ""}"

    override fun execute() {
        if (!options[2].toBool())
            --core.cpu.regs.CTR

        //Not sure, that it really have to be done every time
        if (linkage)
            core.cpu.regs.LR = core.cpu.regs.PC // + 4 // PC already incremented

        val ctr_ok = options[2].toBool() or ((core.cpu.regs.CTR != 0L) xor options[1].toBool())
        val cond_ok = options[4].toBool() or (core.cpu.crBits.bit(condition) == options[3].toBool())
        if (ctr_ok && cond_ok) {
            val extAddr = (address shl 2).ssext(15)
            if (absolute)
                core.cpu.regs.PC = extAddr
            else
                core.cpu.regs.PC += extAddr - 4 // PC already incremented
        }
    }
}