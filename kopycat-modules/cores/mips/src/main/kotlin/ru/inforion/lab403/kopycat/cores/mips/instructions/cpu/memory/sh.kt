package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.memory

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.STORE
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * SH rt, offset(base)
 */
class sh(core: MipsCore,
         data: Long,
         rt: GPR,
         off: MipsDisplacement) : RtOffsetInsn(core, data, Type.VOID, rt, off) {

//    override val store = true
//    override val dtyp = WORD
//    override val core = ProcType.CentralProc
//    override val construct = ::sh
    override val mnem = "sh"

    override fun execute() {
        if (address[0] != 0L)
            throw MemoryAccessError(core.pc, address, STORE, "ADES")
        memword = vrt[15..0]
    }
}
