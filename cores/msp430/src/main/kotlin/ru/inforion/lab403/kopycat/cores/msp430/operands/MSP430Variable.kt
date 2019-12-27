package ru.inforion.lab403.kopycat.cores.msp430.operands

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core

/**
 * Created by a.kemurdzhian on 13/02/18.
 */

class MSP430Variable(dtyp: Datatype, default: Long = 0) : Variable<MSP430Core>(default, dtyp)