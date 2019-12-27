package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.shift

import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRtSaInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * SRA rd, rt, sa
 *
 * To execute an arithmetic right-shift of a word by a fixed id of bits
 *
 * The contents of the low-order 32-bit word of GPR rt are shifted right, duplicating the sign-bit (bit 31)
 * in the emptied bits; the word result is placed in GPR rd. The bit-shift amount is specified by sa.
 */
class sra(core: MipsCore,
          data: Long,
          rd: GPR,
          rs: GPR,
          sa: MipsImmediate) : RdRtSaInsn(core, data, Type.VOID, rd, rs, sa) {

    override val mnem = "sra"

    override fun execute() {
        vrd = (vrt.toInt() shr vsa).toULong()
    }
}