package ru.inforion.lab403.kopycat.cores.mips.instructions.cop.priveleged

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.mips.instructions.Code19bitInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * TLBR
 */
class tlbr(core: MipsCore,
           data: Long,
           imm: MipsImmediate) : Code19bitInsn(core, data, Type.VOID, imm) {

    override val mnem = "tlbr"

    override fun execute() {
        val i = index.toInt()
        if (i > core.mmu.tlbEntries)
            throw GeneralException("Trying to read TLB register above index: $i > ${core.mmu.tlbEntries}")
        val entry = core.mmu.readTlbEntry(i)
        pageMask = entry.pageMask
        entryHi = entry.entryHi
        entryLo0 = entry.entryLo0
        entryLo1 = entry.entryLo1
        log.warning { "${core.cpu.pc.hex8} -> $mnem $entry" }
    }
}