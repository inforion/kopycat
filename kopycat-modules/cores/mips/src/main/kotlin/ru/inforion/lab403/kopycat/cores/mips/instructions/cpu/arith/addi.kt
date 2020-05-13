package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.arith

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.isIntegerOverflow
import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction.Type.VOID
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtRsImmInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * ADDI rt, rs, immediate
 *
 * To add a constant to a 32-bit integer. If overflow occurs, then trap.
 */
class addi(
        core: MipsCore,
        data: Long,
        rt: GPR,
        rs: GPR,
        imm: MipsImmediate) : RtRsImmInsn(core, data, VOID, rt, rs, imm) {

    override val mnem = "addi"
//    override val isSigned = true

    override fun execute() {
        // MIPS guide is bullshit and cause exception each time if second operand < 0
        // TODO: Refactor legacy operand class usage
        val op1 = vrs.asInt
        val op2 = imm.ssext.asInt
        val res = op1 + op2
        if (isIntegerOverflow(op1, op2, res))
            throw MipsHardwareException.OV(core.pc)
        vrt = res.toULong()
    }
}