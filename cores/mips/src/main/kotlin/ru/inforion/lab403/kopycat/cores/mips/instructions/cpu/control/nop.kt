package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.control

import ru.inforion.lab403.common.extensions.WRONGL
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRtSaInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * To perform no operation.
 *
 * NOP is the assembly idiom used to denote no operation. The actual instruction is interpreted
 * by the hardware as SLL r0, r0, 0.
 */
class nop(
        core: MipsCore,
        data: Long = WRONGL,
        rd: GPR,
        rs: GPR,
        sa: MipsImmediate) : RdRtSaInsn(core, data, Type.VOID, rd, rs, sa) {

    override val mnem = "nop"

    override fun execute() {

    }
}