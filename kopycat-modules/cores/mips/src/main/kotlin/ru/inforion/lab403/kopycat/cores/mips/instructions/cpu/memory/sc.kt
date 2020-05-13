package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.memory

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.STORE
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * SC rt, offset(base)
 */
class sc(core: MipsCore,
         data: Long,
         rt: GPR,
         off: MipsDisplacement) : RtOffsetInsn(core, data, Type.VOID, rt, off) {

//    override val store = true
//    override val dtyp = DWORD
//    override val core = ProcType.CentralProc
//    override val construct = ::sc
    override val mnem = "sc"

    override fun execute() {
        val vAddr = address
        if (vAddr[1..0] != 0L)
            throw MemoryAccessError(core.pc, address, STORE, "ADES")
        if (core.cpu.llbit == 1)
            memword = vrt
        vrt = core.cpu.llbit.toULong()
    }
}