package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.logicCondRegs

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Move condition register field
class mcrf(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "mcrf"

    val bf = fieldA[4..2]
    val bfa = fieldB[4..2]

    override fun execute() {
        core.cpu.crBits.cr(bf).field = core.cpu.crBits.cr(bfa).field
    }
}