package ru.inforion.lab403.kopycat.cores.ppc.instructions

import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



abstract class APPCInstruction(
        core: PPCCore,
        type: Type,
        vararg operands: AOperand<PPCCore>) : AInstruction<PPCCore>(core, type, *operands) {

    override val size = 4
}