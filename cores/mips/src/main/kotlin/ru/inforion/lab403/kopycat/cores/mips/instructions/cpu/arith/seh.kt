package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.arith

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


/**
 * Created by a.gladkikh on 03/06/16.
 *
 * seh rd, rt
 *
 * To sign-extend the least significant halfword of GPR rt and store the value into GPR rd.
 */
class seh(
        core: MipsCore,
        data: Long,
        rd: GPR,
        rt: GPR) : RdRtInsn(core, data, Type.VOID, rd, rt) {

    override val mnem = "seh"

    override fun execute() {
        vrd = signext(vrt[15..0], n = 16).toULong()
    }
}