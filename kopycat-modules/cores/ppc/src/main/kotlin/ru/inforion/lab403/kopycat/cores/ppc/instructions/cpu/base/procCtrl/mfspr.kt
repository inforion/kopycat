package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.procCtrl

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.SPR
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Move from special purpose register
open class mfspr(core: PPCCore, val field: Int, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "mfspr"

    val reg = core.cpu.sprSwap(field)

    override fun execute() {

        when (reg.moveFrom) {
            SPR.Access.no -> GeneralException("Move from special register isn't defined for register \"${reg.name}\" (${reg.id})")
            SPR.Access.hypv -> TODO("Isn't implemented")
            SPR.Access.yes -> {}
        }

        if (reg.isPriveleged && core.cpu.msrBits.PR)
            throw GeneralException("Privileged instruction in problem state")

        op1.value(core, reg.value(core))
    }
}