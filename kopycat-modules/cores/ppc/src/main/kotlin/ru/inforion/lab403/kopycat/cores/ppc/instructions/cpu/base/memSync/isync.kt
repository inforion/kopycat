package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.memSync

import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Instruction synchronize
class isync(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "isync"

    override fun execute() {
        //Nothing to do, because we don't use pipeline
    }
}