package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.storageCtrl

import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Data cache block store
class dcbst(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "dcbst"

    override fun execute() {
        // Memory coherence
    }
}