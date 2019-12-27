package ru.inforion.lab403.kopycat.cores.mips.instructions.cop.priveleged

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.clearBit
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.mips.enums.SRSCtl
import ru.inforion.lab403.kopycat.cores.mips.enums.Status
import ru.inforion.lab403.kopycat.cores.mips.instructions.Code19bitInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


/**
 * Created by a.gladkikh on 03/06/16.
 *
 * ERET
 */
class eret(core: MipsCore,
           data: Long,
           imm: MipsImmediate) : Code19bitInsn(core, data, Type.IRET, imm) {

    override val mnem = "eret"

    override fun execute() {
        val pc = if (core.cop.regs.Status[Status.ERL.pos] == 1L) {
            core.cop.regs.Status = clearBit(core.cop.regs.Status, Status.ERL.pos)
            core.cop.regs.ErrorEPC
        } else {
            val StatusBEV = core.cop.regs.Status[Status.BEV.pos].asInt
            val SRSCtlHSS = core.cop.regs.SRSCtl[SRSCtl.HSS.range]
            if (core.ArchitectureRevision >= 2 && SRSCtlHSS > 0 && StatusBEV == 0) {
                // SRSCtlCSS = SRSCtlPSS
                val SRSCtlPSS = core.cop.regs.SRSCtl[SRSCtl.PSS.range]
                core.cop.regs.SRSCtl = core.cop.regs.SRSCtl.insert(SRSCtlPSS, SRSCtl.CSS.range)
            }
            core.cop.regs.Status = clearBit(core.cop.regs.Status, Status.EXL.pos)
            core.cop.regs.EPC
        }
        core.cpu.branchCntrl.jump(pc)
        core.cpu.llbit = 0
        // ClearHazards()
    }
}
