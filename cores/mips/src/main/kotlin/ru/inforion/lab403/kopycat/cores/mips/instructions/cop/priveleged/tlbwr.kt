package ru.inforion.lab403.kopycat.cores.mips.instructions.cop.priveleged

import ru.inforion.lab403.kopycat.cores.mips.instructions.Code19bitInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * TLBWR
 */
class tlbwr(core: MipsCore,
            data: Long,
            imm: MipsImmediate) : Code19bitInsn(core, data, Type.VOID, imm) {

    override val mnem = "tlbwr"

    override fun execute() {
        core.mmu.invalidateCache()
//        val i = core.cop.regs.Random.asInt  // pay attention to Wired register!
        val i = core.mmu.getFreeTlbIndex()
        val entry = core.mmu.writeTlbEntry(i, pageMask, entryHi, entryLo0, entryLo1)
//        log.severe { "${core.cpu.pc.hex8} -> $mnem $entry" }
    }
}

