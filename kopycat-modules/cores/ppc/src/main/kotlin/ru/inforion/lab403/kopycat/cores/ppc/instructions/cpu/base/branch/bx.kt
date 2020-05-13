package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.branch

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.instructions.ssext
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Branch
class bx(core: PPCCore, val address: Long, val absolute: Boolean, val linkage: Boolean):
        APPCInstruction(core, Type.CALL) {
    override val mnem = "b${if (linkage) "l" else ""}${if (absolute) "a" else ""}"
    override fun toString(): String = "$mnem ${address.hex8}"

    override fun execute() {
        if (linkage)
            core.cpu.regs.LR = core.cpu.regs.PC // + 4 // PC already incremented

        val extAddr = (address shl 2).ssext(25)
        if (absolute)
            core.cpu.regs.PC = extAddr
        else
            core.cpu.regs.PC += extAddr - 4 // PC already incremented
    }
}