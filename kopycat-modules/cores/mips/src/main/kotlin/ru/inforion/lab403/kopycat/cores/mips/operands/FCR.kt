package ru.inforion.lab403.kopycat.cores.mips.operands

import ru.inforion.lab403.common.extensions.first
import ru.inforion.lab403.kopycat.cores.mips.enums.Designation
import ru.inforion.lab403.kopycat.cores.mips.enums.eFCR
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.ProcType
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

open class FCR(desc: eFCR) : MipsRegister<eFCR>(ProcType.FloatingPointCop, Designation.Control, desc) {
    constructor(id: Int) : this(first<eFCR> { it.id == id })

    override fun value(core: MipsCore, data: Long) = core.fpu.cntrls.writeIntern(reg, data)
    override fun value(core: MipsCore): Long = core.fpu.cntrls.readIntern(reg)

    object fir : FCR(eFCR.FIR)
    object fccr : FCR(eFCR.FCCR)
    object fexr : FCR(eFCR.FEXR)
    object fenr : FCR(eFCR.FENR)
    object fcsr : FCR(eFCR.FCSR)
}