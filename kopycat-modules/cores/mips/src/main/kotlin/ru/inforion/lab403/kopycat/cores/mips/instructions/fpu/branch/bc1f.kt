package ru.inforion.lab403.kopycat.cores.mips.instructions.fpu.branch

import ru.inforion.lab403.kopycat.cores.mips.instructions.CcOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsNear
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * BC1F cc, offset
 */
class bc1f(core: MipsCore,
           data: Long,
           imm: MipsImmediate,
           off: MipsNear) : CcOffsetInsn(core, data, Type.VOID, imm, off) {

    override val mnem = "bc1f"

    override fun execute() {
//        log.warning { "[%08X] $mnem $cc".format(cpu.pc) }
        if (!vcc) {
            core.cpu.branchCntrl.schedule(address)
        } else {
            core.cpu.branchCntrl.nop()
        }
    }
}
