package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.logicCondRegs

import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Condition register OR
class cror(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "cror"

    override fun execute() {
        val crBit = core.cpu.crBits.bit(fieldB) or core.cpu.crBits.bit(fieldC)
        core.cpu.crBits.bit(fieldA, crBit)
    }
}