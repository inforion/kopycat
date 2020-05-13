package ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.arith

import ru.inforion.lab403.common.extensions.ieee754
import ru.inforion.lab403.kopycat.cores.mips.enums.COND
import ru.inforion.lab403.kopycat.cores.mips.instructions.CcFsFtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.FPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


class c_cond_d(core: MipsCore,
               data: Long,
               fs: FPR,
               ft: FPR,
               cc: MipsImmediate) : CcFsFtInsn(core, data, Type.VOID, fs, ft, cc) {

    override val mnem get() = "c.$cond.d".toLowerCase()

    override fun execute() {
//        log.warning { "[%08X] $mnem $op1:$op2, $op3:$op4 [${dfs.ieee754()} $cond ${dft.ieee754()}]".format(cpu.pc) }
        when (cond) {
            COND.F -> { vcc = false }
            COND.UN -> { vcc = false }
            COND.EQ -> { vcc = dfs.ieee754() == dft.ieee754() }
            COND.UEQ -> { vcc = dfs.ieee754() == dft.ieee754() }
            COND.OLT -> { vcc = dfs.ieee754() < dft.ieee754() }
            COND.ULT -> { vcc = dfs.ieee754() < dft.ieee754() }
            COND.OLE -> { vcc = dfs.ieee754() <= dft.ieee754() }
            // FIXME: WTF???
            COND.ULE -> { dfs.ieee754() <= dft.ieee754() }
            COND.SF -> { vcc = false }
            COND.NGLE -> { vcc = false }
            COND.SEQ -> { vcc = dfs.ieee754() == dft.ieee754() }
            COND.NGL -> { vcc = dfs.ieee754() == dft.ieee754() }
            COND.LT -> { vcc = dfs.ieee754() < dft.ieee754() }
            COND.NGE -> { vcc = dfs.ieee754() < dft.ieee754() }
            COND.LE -> { vcc = dfs.ieee754() <= dft.ieee754() }
            COND.NGT -> { vcc = dfs.ieee754() <= dft.ieee754() }
        }
    }
}
