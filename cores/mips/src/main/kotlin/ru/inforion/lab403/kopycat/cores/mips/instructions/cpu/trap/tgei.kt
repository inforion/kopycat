package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.trap

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.instructions.RsImmInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * TGEI rs, immediate
 *
 * To compare a GPR to a constant and do a conditional trap
 *
 * Compare the contents of GPR rs and the 16-bit signed immediate as signed integers; if GPR rs is greater than
 * or equal to immediate, then take a Trap exception.
 */
class tgei(core: MipsCore,
           data: Long,
           rs: GPR,
           imm: MipsImmediate) : RsImmInsn(core, data, Type.VOID, rs, imm) {

    override val mnem = "tgei"

    override fun execute() {
        // Compare as signed integers
        // TODO: Refactor legacy operand class usage
        if (vrs.asInt >= imm.ssext.asInt) throw MipsHardwareException.TR(core.pc)
    }

}