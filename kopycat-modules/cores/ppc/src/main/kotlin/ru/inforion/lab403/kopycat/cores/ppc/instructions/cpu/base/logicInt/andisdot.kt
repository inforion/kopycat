package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.logicInt

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCVariable
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//AND immediate shifted
class andisdot(core: PPCCore, val condRegField: Long, val length: Boolean, val data: Long, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "andis."

    private val result = PPCVariable(Datatype.DWORD)

    override fun execute() {
        val extImm = data shl 16
        result.value(core, op1.value(core) and extImm)

        op2.value(core, result)
        FlagProcessor.processCR0(core, result)
    }
}