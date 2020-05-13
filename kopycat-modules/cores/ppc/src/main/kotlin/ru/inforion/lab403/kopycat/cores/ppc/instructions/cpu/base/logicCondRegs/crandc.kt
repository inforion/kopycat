package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.logicCondRegs

import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Condition register AND with complement
class crandc(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "crandc"

    override fun execute() {
        val crBit = core.cpu.crBits.bit(fieldB) and (!core.cpu.crBits.bit(fieldC))
        core.cpu.crBits.bit(fieldA, crBit)
    }
}