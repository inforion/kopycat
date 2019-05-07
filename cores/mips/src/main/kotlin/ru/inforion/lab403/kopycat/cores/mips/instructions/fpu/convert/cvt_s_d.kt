package ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.convert

import ru.inforion.lab403.common.extensions.ieee754
import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.kopycat.cores.mips.instructions.FdFsInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.FPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by ra on 08.02.2017.
 *
 * CVT.D.(S,W,L) fd, fs
 *
 * Floating Point Convert to Double Floating Point
 */
class cvt_s_d(
        core: MipsCore,
        data: Long,
        fd: FPR,
        fs: FPR
) : FdFsInsn(core, data, Type.VOID, fd, fs) {

    override val mnem = "cvt.s.d"

    override fun execute() {
        val double = dfs.ieee754()
        val single = double.toFloat()
        vfd = single.ieee754().toULong()
//        log.warning { "[%08X] $mnem $op1 = $single".format(cpu.pc) }
    }
}
