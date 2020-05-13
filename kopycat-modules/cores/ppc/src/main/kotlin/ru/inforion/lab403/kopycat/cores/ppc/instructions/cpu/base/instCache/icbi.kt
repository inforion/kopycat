package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.instCache

import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Data cache block store
class icbi(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "icbi"

    override fun execute() {
        // Cache invalidation
    }
}