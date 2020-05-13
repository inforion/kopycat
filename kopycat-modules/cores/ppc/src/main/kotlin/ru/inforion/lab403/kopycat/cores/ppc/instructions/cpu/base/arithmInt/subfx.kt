package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.arithmInt

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCVariable
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Subtract from
class subfx(core: PPCCore, val overflow: Boolean, val record: Boolean, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "subf${if (overflow) "o" else ""}${if (record) "." else ""}"

    private val result = PPCVariable(Datatype.DWORD)

    override fun execute() {
        result.value(core, op2.inv(core) + op3.value(core) + 1L)

        if (record)
            FlagProcessor.processCR0(core, result)

        if (overflow)
            FlagProcessor.processOverflow(core, result)

        op1.value(core, result)
    }
}