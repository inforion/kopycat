package ru.inforion.lab403.kopycat.cores.x86.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.x86.enums.SSR
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class SSBank(core: x86Core) : ARegistersBank<x86Core, SSR>(core, SSR.values(), bits = 32) {
    override val name: String = "Segment Selector Registers"

    var cs by valueOf(x86Register.SSR.cs)
    var ds by valueOf(x86Register.SSR.ds)
    var ss by valueOf(x86Register.SSR.ss)
    var es by valueOf(x86Register.SSR.es)
    var fs by valueOf(x86Register.SSR.fs)
    var gs by valueOf(x86Register.SSR.gs)

    override fun reset() {
        super.reset()
        cs = 0xFFFF000
        ds = 0x0000000  // perhaps must be cs = 0xFFFF000
    }
}