package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.ppc.exceptions.PPCExceptionHolder_e500v2
import ru.inforion.lab403.kopycat.cores.ppc.hardware.processors.systems.PPCCPU_e500v2
import ru.inforion.lab403.kopycat.cores.ppc.hardware.processors.systems.PPCMMU_EmbeddedMMUFSL



/*
*  +----------------------------------------------------------------------------+
*  |                                                                            |
*  |  +---------+                      +-----------------+                      |
*  |  |         |                      |                 |                      |
*  |  | PPCCPU [>]====Internal bus====[X] PPCMMU [E.MF] [X]====External bus====[X] Proxy
*  |  |         |                      |                 |                      |
*  |  +---------+                      +-----------------+                      |
*  |  +---------+                                                               |
*  |  |         |                                                               |
*  |  | PPCCOP  |                                                               |
*  |  |         |                                                               |
*  |  +---------+                                                               |
*  |                                                                 e500^2 core|
*  +----------------------------------------------------------------------------+
* */
class E500v2(parent: Module, name: String, frequency: Long):
        PPCCoreEmbedded(parent, name, frequency, PPCExceptionHolder_e500v2, ::PPCCPU_e500v2) {

    override val mmu = PPCMMU_EmbeddedMMUFSL(this, "mmu")

    init {
        initRoutine()
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super.serialize(ctxt) + mapOf(
                "ppcemb" to super.serialize(ctxt),
                "mmu" to mmu.serialize(ctxt)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot["ppcemb"] as Map<String, String>)
        mmu.deserialize(ctxt, snapshot["mmu"] as Map<String, String>)
    }
}