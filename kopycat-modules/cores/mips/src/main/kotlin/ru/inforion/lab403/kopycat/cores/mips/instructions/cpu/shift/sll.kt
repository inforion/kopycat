package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.shift

import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRtSaInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * SLL rd, rt, sa
 *
 * To left-shift a word by a fixed id of bits
 *
 * The contents of the low-order 32-bit word of GPR rt are shifted left, inserting zeros into the emptied bits;
 * the word result is placed in GPR rd. The bit-shift amount is specified by sa.
 */
class sll(core: MipsCore,
          data: Long,
          rd: GPR,
          rs: GPR,
          sa: MipsImmediate) : RdRtSaInsn(core, data, Type.VOID, rd, rs, sa) {

    override val mnem = "sll"

    override fun execute() {
        vrd = vrt shl vsa
    }
}