package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.memory

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by batman on 03/06/16.
 *
 * SB rt, offset(base)
 */
class sb(core: MipsCore,
         data: Long,
         rt: GPR,
         off: MipsDisplacement) : RtOffsetInsn(core, data, Type.VOID, rt, off) {

//    override val store = true
//    override val dtyp = BYTE
//    override val core = ProcType.CentralProc
//    override val construct = ::sb
    override val mnem = "sb"

    override fun execute() {
        // I hate mips... and big-endian unsupported
        memword = vrt[7..0]
    }
}