package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.control

import ru.inforion.lab403.common.extensions.WRONGL
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRtSaInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * Break superscalar issue on a superscalar processor.
 *
 * SSNOP is the assembly idiom used to denote superscalar no operation. The actual instruction is interpreted by the
 * hardware as SLL r0, r0, 1.This instruction alters the instruction issue behavior on a superscalar processor by
 * forcing the SSNOP instruction to single-issue. The processor must then end the current instruction issue between
 * the instruction previous to the SSNOP and the SSNOP. The SSNOP then issues alone in the next issue slot. On a
 * single-issue processor, this instruction is a NOP that takes an issue slot.
 */
class ssnop(
        core: MipsCore,
        data: Long = WRONGL,
        rd: GPR,
        rs: GPR,
        sa: MipsImmediate) : RdRtSaInsn(core, data, Type.VOID, rd, rs, sa) {

    override val mnem = "ssnop"

    override fun execute() {

    }
}