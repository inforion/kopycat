package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.memBarier

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Synchronize
class sync(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "sync"

    override fun toString(): String = "$mnem ${fieldA[0]}"

    override fun execute() {
        //Nothing to do, because we don't use pipeline
    }
}