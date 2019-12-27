package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.arith

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction.Type.VOID
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRsRtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 */
class clo(
        core: MipsCore,
        data: Long,
        rd: GPR,
        rs: GPR,
        rt: GPR) : RdRsRtInsn(core, data, VOID, rd, rs, rt) {

    override val mnem = "clo"

    override fun execute() {
        var tmp = 32L
        for (k in 31 downTo 0) {
            if (vrs[k] == 0L) {
                tmp = 31L - k
                break
            }
        }
        vrd = tmp
    }
}