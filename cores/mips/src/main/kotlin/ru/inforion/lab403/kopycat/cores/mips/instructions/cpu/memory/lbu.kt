package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.memory

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03/06/16.
 *
 * LBU rt, offset(base)
 */
class lbu(core: MipsCore,
          data: Long,
          rt: GPR,
          off: MipsDisplacement) : RtOffsetInsn(core, data, Type.VOID, rt, off) {

//    override val store = false
//    override val dtyp = BYTE
//    override val core = ProcType.CentralProc
//    override val construct = ::lbu
    override val mnem = "lbu"

    override fun execute() {
        // I hate mips... and big-endian unsupported
        vrt = memword[7..0]
    }
}
