package ru.inforion.lab403.kopycat.cores.msp430.operands

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core



class MSP430Variable(dtyp: Datatype, default: Long = 0) : Variable<MSP430Core>(default, dtyp)