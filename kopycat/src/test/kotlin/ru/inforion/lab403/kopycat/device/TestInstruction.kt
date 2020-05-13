package ru.inforion.lab403.kopycat.device

import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand

abstract class TestInstruction(
        core: TestCore,
        type: Type,
        override val size: Int,
        vararg operands: AOperand<TestCore>) : AInstruction<TestCore>(core, type, *operands)