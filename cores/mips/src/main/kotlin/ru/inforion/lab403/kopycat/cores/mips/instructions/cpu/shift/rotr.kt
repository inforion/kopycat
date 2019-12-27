package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.shift

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRtSaInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * ROTR rd, rt, sa
 *
 * To execute a logical right-rotate of a word by a fixed id of bits
 */
class rotr(core: MipsCore,
           data: Long,
           rd: GPR,
           rs: GPR,
           sa: MipsImmediate) : RdRtSaInsn(core, data, Type.VOID, rd, rs, sa) {

    override val mnem = "rotr"

    override fun execute() {
        vrd = vrt[vsa - 1..0].shl(32 - vsa) or vrt[31..vsa]
    }

}