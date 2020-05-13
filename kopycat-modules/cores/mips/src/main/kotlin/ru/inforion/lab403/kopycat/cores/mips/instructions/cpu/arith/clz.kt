package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.arith

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction.Type.VOID
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRsRtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


class clz(
        core: MipsCore,
        data: Long,
        rd: GPR,
        rs: GPR,
        rt: GPR) : RdRsRtInsn(core, data, VOID, rd, rs, rt)  {

    override val mnem = "clz"

    override fun execute() {
        var tmp = 32L
        for (k in 31 downTo 0) {
            if (vrs[k] == 1L) {
                tmp = 31L - k
                break
            }
        }
        vrd = tmp
    }
}