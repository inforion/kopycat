package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.arith

import ru.inforion.lab403.common.extensions.WRONGL
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction.Type.VOID
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRsRtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by batman on 03/06/16.
 *
 * ADDU rd, rs, rt
 *
 * To add 32-bit integers
 */
class addu(
        core: MipsCore,
        data: Long = WRONGL,
        rd: GPR,
        rs: GPR,
        rt: GPR) : RdRsRtInsn(core, data, VOID, rd, rs, rt) {

    override val mnem = "addu"

    override fun execute() {
        vrd = vrs + vrt
    }
}