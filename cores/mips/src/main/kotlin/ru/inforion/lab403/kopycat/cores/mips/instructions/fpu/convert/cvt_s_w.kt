package ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.convert

import ru.inforion.lab403.common.extensions.ieee754
import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.kopycat.cores.mips.instructions.FdFsInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.FPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by r.aristov on 08.02.2017.
 *
 * CVT.D.(S,W,L) fd, fs
 *
 * Floating Point Convert to Double Floating Point
 */
class cvt_s_w(
        core: MipsCore,
        data: Long,
        fd: FPR,
        fs: FPR
) : FdFsInsn(core, data, Type.VOID, fd, fs) {

    override val mnem = "cvt.s.w"

    override fun execute() {
        val single = vfs.toFloat()
        val int = single.ieee754()
        vfd = int.toULong()
//        log.warning { "[%08X] $mnem $op1:$op2 = $int".format(cpu.pc) }
    }
}
