package ru.inforion.lab403.kopycat.cores.arm.operands

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by the bat on 13.01.18.
 */

fun ARMVariable(dtyp: Datatype, default: Long = 0) = Variable<AARMCore>(default, dtyp)