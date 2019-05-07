package ru.inforion.lab403.kopycat.cores.msp430.operands

import ru.inforion.lab403.common.extensions.WRONGI
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.Memory
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core

/**
 * Created by shiftdj on 9/02/18.
 */

class MSP430Memory(dtyp : Datatype, access : AOperand.Access, addr: Long) : Memory<MSP430Core>(dtyp, WORD, addr, access, WRONGI)
