package ru.inforion.lab403.kopycat.cores.mips.instructions.cop.priveleged

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * RDPGPR rd, rt
 */
class rdpgpr(core: MipsCore,
             data: Long,
             rd: GPR,
             rt: GPR) : RdRtInsn(core, data, Type.VOID, rd, rt) {

    override val mnem = "rdpgpr"

    override fun execute() {
        val pss = core.cop.regs.SRSCtl[9..6].toInt()
        val shadow = core.cpu.sgprs[pss]
        // FIXME: Remove read intern here rd.value should used
        vrd = shadow.readIntern(rt.reg)
    }
}

