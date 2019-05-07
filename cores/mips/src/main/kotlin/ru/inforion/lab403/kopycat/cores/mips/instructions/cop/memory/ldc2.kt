package ru.inforion.lab403.kopycat.cores.mips.instructions.cop.memory

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.mips.instructions.FtOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by batman on 03/06/16.
 *
 * LDC2 ft, offset(base)
 */
class ldc2(
        core: MipsCore,
        data: Long,
        ct: MipsRegister<*>,
        off: MipsDisplacement) : FtOffsetInsn(core, data, Type.VOID, ct, off) {

//    override val store = false
//    override val dtyp = Datatype.QWORD
//    override val core = ProcType.ImplementSpecCop
    override val mnem = "ldc2"

    override fun execute() {
        throw GeneralException("Sorry, but I don't know how to execute this instruction!")
    }
}
