package ru.inforion.lab403.kopycat.cores.mips.instructions.cop.priveleged

import ru.inforion.lab403.kopycat.cores.mips.instructions.Code19bitInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by batman on 03/06/16.
 *
 * TLBWI
 */
class tlbwi(core: MipsCore,
            data: Long,
            imm: MipsImmediate) : Code19bitInsn(core, data, Type.VOID, imm) {

    override val mnem = "tlbwi"

    override fun execute() {
        core.mmu.invalidateCache()
        val i = index.toInt()
        core.mmu.writeTlbEntry(i, pageMask, entryHi, entryLo0, entryLo1)
//        log.warning { "${core.cpu.pc.hex8} -> $mnem ${core.mmu.readTlbEntry(i)}" }
    }
}
