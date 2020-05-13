package ru.inforion.lab403.kopycat.cores.arm.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class VERBank : ARegisterBankNG(32) {
    override val name: String = "Virtualization Extension Registers"

    class Operand(reg: Int, access: Access = Access.ANY) : ARegister<AARMCore>(reg, access) {
        override fun toString(): String = "VER[$reg]" // TODO: replace it
        override fun value(core: AARMCore, data: Long) = core.cpu.ver.write(reg, data)
        override fun value(core: AARMCore): Long = core.cpu.ver.read(reg)
    }

    inner class HCR : Register() {
        var vm by bitOf(0)
        var swio by bitOf(1)
        var ptw by bitOf(2)
        var fmo by bitOf(3)
        var imo by bitOf(4)
        var amo by bitOf(5)
        var vf by bitOf(6)
        var vi by bitOf(7)
        var va by bitOf(8)
        var fb by bitOf(9)
        var bsu by fieldOf(11, 10)
        var dc by bitOf(12)
        var twi by bitOf(13)
        var twe by bitOf(14)
        var tid0 by bitOf(15)
        var tid1 by bitOf(16)
        var tid2 by bitOf(17)
        var tid3 by bitOf(18)
        var tsc by bitOf(19)
        var tidcp by bitOf(20)
        var tac by bitOf(21)
        var tsw by bitOf(22)
        var tpc by bitOf(23)
        var tpu by bitOf(24)
        var ttlb by bitOf(25)
        var tvm by bitOf(26)
        var tge by bitOf(27)
    }

    val hcr = HCR()
    val hsr = Register()
    val hstr = Register()

    init {
        initialize()
    }
}