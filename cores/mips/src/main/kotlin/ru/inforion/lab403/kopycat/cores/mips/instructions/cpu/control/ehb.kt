package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.control

import ru.inforion.lab403.common.extensions.WRONGL
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRtSaInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * To stop instruction execution until all execution hazards have been cleared.
 *
 * EHB is the assembly idiom used to denote execution hazard barrier. The actual instruction is interpreted by the
 * hardware as SLL r0, r0, 3. This instruction alters the instruction issue behavior on a pipelined processor by
 * stopping execution until all execution hazards have been cleared. Other than those that might be created as a
 * consequence of setting StatusCU0, there are no execution hazards visible to an unprivileged program running in
 * User Mode. All execution hazards created by previous instructions are cleared for instructions executed immediately
 * following the EHB, even if the EHB is executed in the delay slot of a branch or jump. The EHB instruction does not
 * clear instruction hazards - such hazards are cleared by the JALR.HB, JR.HB, and ERET instructions.
 */
class ehb(
        core: MipsCore,
        data: Long = WRONGL,
        rd: GPR,
        rs: GPR,
        sa: MipsImmediate) : RdRtSaInsn(core, data, Type.VOID, rd, rs, sa) {

    override val mnem = "ehb"

    override fun execute() {
        // TODO: ClearExecutionHazards()
    }

}