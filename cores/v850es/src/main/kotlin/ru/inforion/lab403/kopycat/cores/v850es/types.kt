package ru.inforion.lab403.kopycat.cores.v850es

import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by batman on 22/07/17.
 */
typealias constructor = (v850ESCore, Int, Array<AOperand<v850ESCore>>) -> AV850ESInstruction
typealias v850ESOperand = AOperand<v850ESCore>
typealias v850ESInstruction = AInstruction<v850ESCore>