package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.embedded.tlbmanage

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//TLB Invalidate Virtual Address Indexed
class tlbivax(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "tlbivax"

    val ra = PPCRegister.gpr(fieldB)
    val rb = PPCRegister.gpr(fieldC)

    override fun execute() {
        val a = if (fieldA == 0) 0 else ra.value(core)
        val ea = a + rb.value(core)
        val tlb = ea[4..3].toInt()
        for (entry in core.mmu.TLB[tlb]) {
            val m = ((1L shl (2L * (entry.SIZE - 1L)).toInt()) - 1L).inv()
            if (((ea[63..12] and m) == (entry.EPN and m)) || ea[2].toBool())
                if (!entry.IPROT)
                    entry.V = false
        }
    }
}