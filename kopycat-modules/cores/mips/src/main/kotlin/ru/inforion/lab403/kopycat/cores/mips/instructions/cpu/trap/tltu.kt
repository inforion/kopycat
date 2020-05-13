package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.trap

import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.instructions.RsRtCodeInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * TLTU rs, rt
 *
 * To compare a GPR to a constant and do a conditional trap
 *
 * Compare the contents of GPR rs and GPR rt as unsigned integers; if GPR rs is less than GPR rt, then take a
 * Trap exception. The contents of the code field are ignored by hardware and may be used to encode information
 * for system software. To retrieve the information, system software must load the instruction word from memory.
 */
class tltu(core: MipsCore,
           data: Long,
           rs: GPR,
           rt: GPR,
           code: MipsImmediate) : RsRtCodeInsn(core, data, Type.VOID, rs, rt, code) {

    override val mnem = "tltu"

    override fun execute() {
        // Compare as unsigned integers
        if (vrs < vrt) throw MipsHardwareException.TR(core.pc)
    }

}