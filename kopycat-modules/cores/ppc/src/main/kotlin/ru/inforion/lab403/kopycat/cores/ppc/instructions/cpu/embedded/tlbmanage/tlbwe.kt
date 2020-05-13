package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.embedded.tlbmanage

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl.eMAS0
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl.eMAS3
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl.eSPR_EmbeddedMMUFSL
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//TLB Write Entry
class tlbwe(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "tlbwe"

    override fun execute() {
        val mas0 = core.cpu.spr(eSPR_EmbeddedMMUFSL.MAS0.id).value(core)
        val mas1 = core.cpu.spr(eSPR_EmbeddedMMUFSL.MAS1.id).value(core)
        val mas2 = core.cpu.spr(eSPR_EmbeddedMMUFSL.MAS2.id).value(core)
        val mas3 = core.cpu.spr(eSPR_EmbeddedMMUFSL.MAS3.id).value(core)
        val mas7 = core.cpu.spr(eSPR_EmbeddedMMUFSL.MAS7.id).value(core)
        val tlb = mas0[eMAS0.TLBSEL].toInt()
        val ent = mas0[eMAS0.ESEL].toInt()
        val n = eMAS3.RPNL.first - eMAS3.RPNL.last + 1
        val rpn = (mas7 shl n) or mas3[eMAS3.RPNL]
        core.mmu.tlbWrite(tlb, ent,  mas1, mas2, mas3, rpn)
    }
}