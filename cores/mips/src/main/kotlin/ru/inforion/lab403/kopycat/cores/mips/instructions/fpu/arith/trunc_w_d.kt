package ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.arith

import ru.inforion.lab403.common.extensions.ieee754
import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.kopycat.cores.mips.instructions.FdFsInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.FPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by r.aristov on 08.02.2017.
 *
 * TRUNC.W.(S,D) fd, fs
 *
 * Floating Point Convert to Double Floating Point
 */
class trunc_w_d(core: MipsCore,
                data: Long,
                fd: FPR,
                fs: FPR) : FdFsInsn(core, data, Type.VOID, fd, fs) {

    override val mnem = "trunc.w.d"

    override fun execute() {
        val double = dfs.ieee754()
        val int = double.toInt()
        vfd = int.toULong()
//        log.warning { "[%08X] $mnem $op1, $op3 [$double -> $int]".format(cpu.pc) }
    }
}
