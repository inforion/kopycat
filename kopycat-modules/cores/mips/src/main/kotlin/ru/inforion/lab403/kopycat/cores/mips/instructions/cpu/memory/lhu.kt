package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.memory

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.LOAD
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * LHU rt, offset(base)
 */
class lhu(core: MipsCore,
          data: Long,
          rt: GPR,
          off: MipsDisplacement) : RtOffsetInsn(core, data, Type.VOID, rt, off) {

//    override val store = false
//    override val dtyp = WORD
//    override val core = ProcType.CentralProc
//    override val construct = ::lhu
    override val mnem = "lhu"

    override fun execute() {
        // I hate mips... and big-endian unsupported
        if (address[0] != 0L)
            throw MemoryAccessError(core.pc, address, LOAD, "ADEL")
        vrt = memword[15..0]
    }
}
