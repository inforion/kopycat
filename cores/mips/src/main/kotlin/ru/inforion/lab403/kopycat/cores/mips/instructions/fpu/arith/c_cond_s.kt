package ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.arith

import ru.inforion.lab403.common.extensions.ieee754
import ru.inforion.lab403.kopycat.cores.mips.enums.COND
import ru.inforion.lab403.kopycat.cores.mips.instructions.CcFsFtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.FPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 11.04.17.
 */
class c_cond_s(
        core: MipsCore,
        data: Long,
        fs: FPR,
        ft: FPR,
        cc: MipsImmediate) : CcFsFtInsn(core, data, Type.VOID, fs, ft, cc) {

    override val mnem get() = "c.$cond.s".toLowerCase()

    override fun execute() {
//        log.warning { "[%08X] $mnem $op1, $op3 [$fs $cond $vft]".format(cpu.pc) }
        when (cond) {
            COND.F -> { vcc = false }
            COND.UN -> { vcc = false }
            COND.EQ -> { vcc = vfs.ieee754() == vft.ieee754() }
            COND.UEQ -> { vcc = vfs.ieee754() == vft.ieee754() }
            COND.OLT -> { vcc = vfs.ieee754() < vft.ieee754() }
            COND.ULT -> { vcc = vfs.ieee754() < vft.ieee754() }
            COND.OLE -> { vcc = vfs.ieee754() <= vft.ieee754() }
            // FIXME: WTF???
            COND.ULE -> { vfs.ieee754() <= vft.ieee754() }
            COND.SF -> { vcc = false }
            COND.NGLE -> { vcc = false }
            COND.SEQ -> { vcc = vfs.ieee754() == vft.ieee754() }
            COND.NGL -> { vcc = vfs.ieee754() == vft.ieee754() }
            COND.LT -> { vcc = vfs.ieee754() < vft.ieee754() }
            COND.NGE -> { vcc = vfs.ieee754() < vft.ieee754() }
            COND.LE -> { vcc = vfs.ieee754() <= vft.ieee754() }
            COND.NGT -> { vcc = vfs.ieee754() <= vft.ieee754() }
        }
    }
}
