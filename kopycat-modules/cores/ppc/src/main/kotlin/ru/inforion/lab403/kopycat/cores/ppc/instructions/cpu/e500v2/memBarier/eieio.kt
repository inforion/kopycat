package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.e500v2.memBarier

import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Move to special purpose register
class eieio(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "eieio"


    override fun execute() {
        //Nothing to do, because we don't use pipeline
    }
}