package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.trap

import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.instructions.RsRtCodeInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * TLT rs, rt
 *
 * To compare a GPR to a constant and do a conditional trap
 *
 * Compare the contents of GPR rs and GPR rt as signed integers; if GPR rs is less than GPR rt, then take a
 * Trap exception. The contents of the code field are ignored by hardware and may be used to encode information
 * for system software. To retrieve the information, system software must load the instruction word from memory.
 */
class tlt(core: MipsCore,
          data: Long,
          rs: GPR,
          rt: GPR,
          code: MipsImmediate) : RsRtCodeInsn(core, data, Type.VOID, rs, rt, code) {

    override val mnem = "tlt"

    override fun execute() {
        // Compare as signed integers
        if (vrs.toInt() < vrt.toInt()) throw MipsHardwareException.TR(core.pc)
    }

}