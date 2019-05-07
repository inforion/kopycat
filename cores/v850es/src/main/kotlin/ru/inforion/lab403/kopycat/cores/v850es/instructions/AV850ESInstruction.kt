package ru.inforion.lab403.kopycat.cores.v850es.instructions

import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by user on 23.05.17.
 */

abstract class AV850ESInstruction(
        core: v850ESCore,
        type: Type,
        override val size: Int,
        vararg operands: AOperand<v850ESCore>
) : AInstruction<v850ESCore>(core, type, *operands){

    open val cyChg = false
    open val ovChg = false
    open val sChg = false
    open val zChg = false
    open val satChg = false
}
