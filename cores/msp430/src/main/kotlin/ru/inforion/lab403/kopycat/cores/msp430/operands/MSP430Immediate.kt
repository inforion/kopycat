package ru.inforion.lab403.kopycat.cores.msp430.operands

import ru.inforion.lab403.common.extensions.WRONGI
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core

/**
 * Created by a.kemurdzhian on 6/02/18.
 */

class MSP430Immediate(dtyp: Datatype, value: Long, signed: Boolean) : Immediate<MSP430Core>(value, signed, dtyp, WRONGI)
fun zero(dtyp: Datatype) = MSP430Immediate(dtyp, 0, true)
fun one(dtyp: Datatype) = MSP430Immediate(dtyp, 1, true)
fun two(dtyp: Datatype) = MSP430Immediate(dtyp, 2, true)
fun four(dtyp: Datatype) = MSP430Immediate(dtyp, 4, true)
fun eight(dtyp: Datatype) = MSP430Immediate(dtyp, 8, true)
fun negOne(dtyp: Datatype) = MSP430Immediate(dtyp, -1, true)
