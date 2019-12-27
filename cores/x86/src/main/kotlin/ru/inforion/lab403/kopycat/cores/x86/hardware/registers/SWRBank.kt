package ru.inforion.lab403.kopycat.cores.x86.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.x86.enums.SWR
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 09.02.18.
 */

class SWRBank(core: x86Core) : ARegistersBank<x86Core, SWR>(core, SWR.values(), bits = 16) {
    override val name: String = "SWR Register"

    var ie by bitOf(x86Register.FWR.SWR, SWR.IE.bit)
    var de by bitOf(x86Register.FWR.SWR, SWR.DE.bit)
    var xe by bitOf(x86Register.FWR.SWR, SWR.XE.bit)
    var oe by bitOf(x86Register.FWR.SWR, SWR.OE.bit)
    var ue by bitOf(x86Register.FWR.SWR, SWR.UE.bit)
    var pe by bitOf(x86Register.FWR.SWR, SWR.PE.bit)
    var sf by bitOf(x86Register.FWR.SWR, SWR.SF.bit)
    var es by bitOf(x86Register.FWR.SWR, SWR.ES.bit)
    var c0 by bitOf(x86Register.FWR.SWR, SWR.C0.bit)
    var c1 by bitOf(x86Register.FWR.SWR, SWR.C1.bit)
    var c2 by bitOf(x86Register.FWR.SWR, SWR.C2.bit)
    var top by bitOf(x86Register.FWR.SWR, SWR.TOP.bit)
    var c3 by bitOf(x86Register.FWR.SWR, SWR.C3.bit)
    var b by bitOf(x86Register.FWR.SWR, SWR.B.bit)
}