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
 * TNEI rs, immediate
 *
 * To compare a GPR to a constant and do a conditional trap
 *
 * Compare the contents of GPR rs and the 16-bit signed immediate as signed integers; if GPR rs is not equal to
 * immediate, then take a Trap exception.
 */
class tnei(core: MipsCore,
           data: Long,
           rs: GPR,
           imm: MipsImmediate) : RsImmInsn(core, data, Type.VOID, rs, imm) {

//    override val isSigned: Boolean = true
//    override val construct = ::tnei
    override val mnem = "tnei"

    override fun execute() {
        // Compare as signed
        if (vrs.asInt != imm.ssext.asInt) throw MipsHardwareException.TR(core.pc)
    }

}