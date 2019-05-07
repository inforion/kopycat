package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.trap

import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.instructions.RsRtCodeInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by batman on 03/06/16.
 *
 * TGEU rs, rt
 *
 * To compare a GPR to a constant and do a conditional trap
 *
 * Compare the contents of GPR rs and the 16-bit sign-extended immediate as unsigned integers; if GPR rs is
 * greater than or equal to immediate, then take a Trap exception. Because the 16-bit immediate is sign-extended
 * before comparison, the instruction can represent the smallest or largest unsigned numbers.
 * The representable values are at the minimum [0, 32767] or maximum [max_unsigned-32767, max_unsigned]
 * end of the unsigned range.
 */
class tgeu(core: MipsCore,
           data: Long,
           rs: GPR,
           rt: GPR,
           code: MipsImmediate) : RsRtCodeInsn(core, data, Type.VOID, rs, rt, code) {

    override val mnem = "tgeu"

    override fun execute() {
        // Compare as unsigned integers
        if (vrs >= vrt) throw MipsHardwareException.TR(core.pc)
    }

}