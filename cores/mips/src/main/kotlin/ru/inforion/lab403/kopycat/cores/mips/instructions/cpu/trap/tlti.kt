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
 * TLTI rs, immediate
 *
 * To compare a GPR to a constant and do a conditional trap
 *
 * Compare the contents of GPR rs and the 16-bit signed immediate as signed integers; if GPR rs is less than
 * immediate, then take a Trap exception.
 */
class tlti(core: MipsCore,
           data: Long,
           rs: GPR,
           imm: MipsImmediate) : RsImmInsn(core, data, Type.VOID, rs, imm) {

//    override val isSigned: Boolean = true
//    override val construct = ::tlti
    override val mnem = "tlti"

    override fun execute() {
        // Compare as signed integers
        if (vrs.asInt < imm.ssext.asInt) throw MipsHardwareException.TR(core.pc)
    }

}