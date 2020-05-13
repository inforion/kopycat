package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.tlbmanage

import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//TLB Synchronize
class tlbsync(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "tlbsync"

    override fun execute() {
        //Nothing to do, because we don't use pipeline
    }
}