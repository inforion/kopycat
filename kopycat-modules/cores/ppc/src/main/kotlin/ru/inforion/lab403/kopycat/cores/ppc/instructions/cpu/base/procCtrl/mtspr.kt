package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.procCtrl

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.SPR
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Move to special purpose register
class mtspr(core: PPCCore, val field: Int, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "mtspr"

    val reg = core.cpu.sprSwap(field)

    override fun toString(): String = "$mnem ${reg.name}"

    override fun execute() {

        when (reg.moveTo) {
            SPR.Access.no -> throw GeneralException("Move to special register isn't defined for register \"${reg.name}\" (${reg.id})")
            SPR.Access.hypv -> TODO("Isn't implemented")
            SPR.Access.yes -> {}
        }

        if (reg.isPriveleged && core.cpu.msrBits.PR)
            throw GeneralException("Privileged instruction in problem state")

        reg.value(core, op1.value(core))
    }
}