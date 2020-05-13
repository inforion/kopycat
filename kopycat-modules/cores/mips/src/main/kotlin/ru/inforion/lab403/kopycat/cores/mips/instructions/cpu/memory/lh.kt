package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.memory

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.LOAD
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * LH rt, offset(base)
 */
class lh(core: MipsCore,
         data: Long,
         rt: GPR,
         off: MipsDisplacement) : RtOffsetInsn(core, data, Type.VOID, rt, off) {

//    override val store = false
//    override val dtyp = WORD
//    override val core = ProcType.CentralProc
//    override val construct = ::lh
    override val mnem = "lh"

    override fun execute() {
        // I hate mips... and big-endian unsupported
        if (address[0] != 0L)
            throw MemoryAccessError(core.pc, address, LOAD, "ADEL")
        vrt = signext(memword[15..0], n = 16).toULong()
    }
}