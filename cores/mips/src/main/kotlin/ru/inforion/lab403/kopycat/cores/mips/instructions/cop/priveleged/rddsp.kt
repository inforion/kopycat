package ru.inforion.lab403.kopycat.cores.mips.instructions.cop.priveleged

import ru.inforion.lab403.kopycat.cores.mips.instructions.RdInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by the bat on 19/06/17.
 *
 * RDDSP rd - for DSP extension
 */
class rddsp(core: MipsCore,
            data: Long,
            rd: GPR) : RdInsn(core, data, Type.VOID, rd) {

//    override val construct = ::rddsp
    override val mnem = "rddsp"

    override fun execute() {
        if (core.dspExtension)
            log.severe { "dspExtension not implemented!" }
    }
}

