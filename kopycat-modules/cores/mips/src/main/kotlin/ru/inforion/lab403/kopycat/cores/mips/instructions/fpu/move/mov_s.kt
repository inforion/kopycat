package ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.move

import ru.inforion.lab403.kopycat.cores.mips.instructions.FdFsInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.FPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * MOV.(S,D) fd, fs
 *
 * Floating Point Convert to Double Floating Point
 */
class mov_s(
        core: MipsCore,
        data: Long,
        fd: FPR,
        fs: FPR
) : FdFsInsn(core, data, Type.VOID, fd, fs) {

    override val mnem = "mov.s"

    override fun execute() {
//        log.warning { "[%08X] $mnem $op1 = ${fs.ieee754()}".format(cpu.pc) }
        vfd = vfs
    }
}
