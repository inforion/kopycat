package ru.inforion.lab403.kopycat.cores.x86.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.x86.enums.CWR
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class CWRBank(core: x86Core) : ARegistersBank<x86Core, CWR>(core, CWR.values(), bits = 16) {
    override val name: String = "CWR Register"

    var i by bitOf(x86Register.FWR.CWR, CWR.I.bit)
    var d by bitOf(x86Register.FWR.CWR, CWR.D.bit)
    var z by bitOf(x86Register.FWR.CWR, CWR.Z.bit)
    var o by bitOf(x86Register.FWR.CWR, CWR.O.bit)
    var u by bitOf(x86Register.FWR.CWR, CWR.U.bit)
    var p by bitOf(x86Register.FWR.CWR, CWR.P.bit)
    var pc by bitOf(x86Register.FWR.CWR, CWR.PC.bit)
    var rc by bitOf(x86Register.FWR.CWR, CWR.RC.bit)
}