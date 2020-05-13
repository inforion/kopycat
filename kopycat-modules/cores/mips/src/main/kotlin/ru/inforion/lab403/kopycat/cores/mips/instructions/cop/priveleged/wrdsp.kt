package ru.inforion.lab403.kopycat.cores.mips.instructions.cop.priveleged

import ru.inforion.lab403.common.extensions.WRONGL
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction.Type.VOID
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * RDDSP rd - for DSP extension
 */
class wrdsp(core: MipsCore,
            data: Long = WRONGL,
            rd: GPR) : RdInsn(core, data, VOID, rd) {

    override val mnem = "wrdsp"

    override fun execute() {
        if (core.dspExtension)
            log.severe { "dspExtension not implemented!" }
    }
}

