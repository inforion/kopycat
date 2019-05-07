package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.memory

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.STORE
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by batman on 03/06/16.
 *
 * SW rt, offset(base)
 */
class sw(core: MipsCore,
         data: Long,
         rt: GPR,
         off: MipsDisplacement) : RtOffsetInsn(core, data, Type.VOID, rt, off) {

//    override val store = true
//    override val dtyp = DWORD
//    override val core = ProcType.CentralProc
//    override val construct = ::sw
    override val mnem = "sw"

    override fun execute() {
        if (address[1..0] != 0L)
            throw MemoryAccessError(core.pc, address, STORE, "ADES")
        memword = vrt
    }
}
