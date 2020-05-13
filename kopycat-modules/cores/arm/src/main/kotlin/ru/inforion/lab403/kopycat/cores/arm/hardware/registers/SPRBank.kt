package ru.inforion.lab403.kopycat.cores.arm.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class SPRBank : ARegisterBankNG(32) {
    override val name = "ARM Special Purpose Registers Bank"

    class Operand(reg: Int, access: Access = Access.ANY) : ARegister<AARMCore>(reg, access) {
        override fun toString(): String = "SPR[$reg]" // TODO: replace it
        override fun value(core: AARMCore, data: Long) = core.cpu.spr.write(reg, data)
        override fun value(core: AARMCore): Long = core.cpu.spr.read(reg)
    }

    inner class PRIMASK : Register() {
        var pm by bitOf(0)
    }

    inner class CONTROL : Register() {
        var npriv by bitOf(0)
        var spsel by bitOf(1)
    }

    val primask = PRIMASK()
    val control = CONTROL()

    init {
        initialize()
    }
}