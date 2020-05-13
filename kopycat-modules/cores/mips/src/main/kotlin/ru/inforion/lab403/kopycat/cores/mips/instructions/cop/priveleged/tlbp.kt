package ru.inforion.lab403.kopycat.cores.mips.instructions.cop.priveleged

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.MipsMMU
import ru.inforion.lab403.kopycat.cores.mips.instructions.Code19bitInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * TLBP
 */
class tlbp(core: MipsCore,
           data: Long,
           imm: MipsImmediate) : Code19bitInsn(core, data, Type.VOID, imm) {

    override val mnem = "tlbp"

    override fun execute() {
        index = -1
        val match = MipsMMU.TLBEntry(-1, pageMask, entryHi, entryLo0, entryLo1)
        val mask = match.VPN2 and match.Mask.inv()
        for (i in 0 until core.mmu.tlbEntries) {
            val TLB = core.mmu.readTlbEntry(i)
            val cond1 = (TLB.VPN2 and TLB.Mask.inv()) == mask
            val cond2 = TLB.G == 1 || TLB.ASID == match.ASID
            if (cond1 && cond2)
                index = i.asULong
        }
//        log.severe { "${core.cpu.pc.hex8} -> $mnem $index" }
    }
}

