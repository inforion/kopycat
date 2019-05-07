package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.arith

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRsRtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


/**
 * Created by batman on 03/06/16.
 */
class sub(
        core: MipsCore,
        data: Long,
        rd: GPR,
        rs: GPR,
        rt: GPR) : RdRsRtInsn(core, data, Type.VOID, rd, rs, rt)  {

    override val mnem = "sub"

    override fun execute() {
        val op1 = vrs or ((vrs and 0x80000000) shl 1)
        val op2 = vrt or ((vrt and 0x80000000) shl 1)
        val tmp = op1 - op2
        if (tmp[32] != tmp[31])
            throw MipsHardwareException.OV(core.pc)
        vrd = tmp
    }
}