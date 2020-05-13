package ru.inforion.lab403.kopycat.cores.arm.instructions

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



abstract class AARMInstruction(
        core: AARMCore,
        type: Type,
        val cond: Condition,
        val opcode: Long,
        vararg operands: AOperand<AARMCore>,
        override val size: Int = 4) : AInstruction<AARMCore>(core, type, *operands) {

    val mcnd = if (cond != Condition.AL && cond != Condition.UN) "$cond" else ""
}
