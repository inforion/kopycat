package ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.arith

import ru.inforion.lab403.common.extensions.ieee754
import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.kopycat.cores.mips.instructions.FdFsInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.FPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * TRUNC.W.(S,D) fd, fs
 *
 * Floating Point Convert to Double Floating Point
 */
class trunc_w_s(core: MipsCore,
                data: Long,
                fd: FPR,
                fs: FPR) : FdFsInsn(core, data, Type.VOID, fd, fs) {

    override val mnem = "trunc.w.s"

    override fun execute() {
        val single = vfs.ieee754()
        val int = single.toInt()
        vfd = int.toULong()
//        log.warning { "[%08X] $mnem $op1, $op3 [$single -> $int]".format(cpu.pc) }
    }
}
