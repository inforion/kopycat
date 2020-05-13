package ru.inforion.lab403.kopycat.cores.mips.instructions.cop.priveleged

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction.Type.VOID
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * WRPGPR rd, rt
 */
class wrpgpr(
        core: MipsCore,
        data: Long,
        rd: GPR,
        rt: GPR
) : RdRtInsn(core, data, VOID, rd, rt) {

    override val mnem = "wrpgpr"

    override fun execute() {
        val pss = core.cop.regs.SRSCtl[9..6].toInt()
        val shadow = core.cpu.sgprs[pss]
        // FIXME: Remove write intern here rd.value should used
        shadow.writeIntern(rd.reg, vrt)
    }
}
