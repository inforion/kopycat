package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.arithmInt


import ru.inforion.lab403.common.extensions.mask
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.instructions.usext
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCVariable
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Add immediate carrying and record
class addicdot(core: PPCCore, val condRegField: Long, val length: Boolean, val data: Long, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "addic."

    private val result = PPCVariable(Datatype.DWORD)

    override fun execute() {
        val extImm = data.usext(15) // Carry fix
        val rA = op2.value(core)
        result.value(core, rA + extImm)

        op1.value(core, result)

        FlagProcessor.processCarry(core, result)
        FlagProcessor.processCR0(core, result)
    }
}