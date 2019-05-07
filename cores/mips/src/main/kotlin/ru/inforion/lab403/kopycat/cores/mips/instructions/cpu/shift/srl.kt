package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.shift

import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRtSaInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by batman on 03/06/16.
 *
 * SRL rd, rt, sa
 *
 * To execute a logical right-shift of a word by a fixed id of bits
 */
class srl(core: MipsCore,
          data: Long,
          rd: GPR,
          rs: GPR,
          sa: MipsImmediate) : RdRtSaInsn(core, data, Type.VOID, rd, rs, sa) {

    override val mnem = "srl"

    override fun execute() {
        vrd = vrt ushr vsa
    }
}