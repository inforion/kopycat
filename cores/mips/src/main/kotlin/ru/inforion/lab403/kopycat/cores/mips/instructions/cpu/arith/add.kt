package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.arith

import ru.inforion.lab403.common.extensions.isIntegerOverflow
import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRsRtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * ADD rd, rs, rt
 *
 * To add 32-bit integers. If an overflow occurs, then trap.
 *
 * The 32-bit word value in GPR rt is added to the 32-bit value in GPR rs to produce a 32-bit result.
 */
class add(
        core: MipsCore,
        data: Long,
        rd: GPR,
        rs: GPR,
        rt: GPR) : RdRsRtInsn(core, data, Type.VOID, rd, rs, rt) {

    override val mnem = "add"

    override fun execute() {
        // MIPS guide is bullshit and cause exception each time if second operand < 0
        val op1 = vrs.toInt()
        val op2 = vrt.toInt()
        val res = op1 + op2
        if (isIntegerOverflow(op1, op2, res))
            throw MipsHardwareException.OV(core.pc)
        vrd = res.toULong()
    }
}
