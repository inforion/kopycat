package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.embedded.procCtrl

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Move to special purpose register
class mtmsr(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "mtmsr"

    val rs = PPCRegister.gpr(fieldA)
    val L = fieldB[0]

    override fun execute() {
        val value = rs.value(core)
        if (L == 0) {
            core.cpu.msrBits.EE = value[15].toBool() || value[14].toBool()
            core.cpu.msrBits.IS = value[5].toBool() || value[14].toBool()
            core.cpu.msrBits.DS = value[4].toBool() || value[14].toBool()
            core.cpu.msrBits.bits(31..16, value[31..16])
            core.cpu.msrBits.bits(14..13, value[14..13])
            core.cpu.msrBits.bits(11..6, value[11..6])
            core.cpu.msrBits.bits(3..1, value[3..1])
        }
        else {
            core.cpu.msrBits.bits(15..1, value[15..1])
        }
    }
}