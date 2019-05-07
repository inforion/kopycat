package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.arith

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


/**
 * Created by batman on 03/06/16.
 *
 * seb rd, rt
 *
 * To sign-extend the least significant byte of GPR rt and store the value into GPR rd.
 */
class seb(
        core: MipsCore,
        data: Long,
        rd: GPR,
        rt: GPR) : RdRtInsn(core, data, Type.VOID, rd, rt) {

    override val mnem = "seb"

    override fun execute() {
        vrd = signext(vrt[7..0], n = 8).toULong()
    }
}