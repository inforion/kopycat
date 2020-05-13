package ru.inforion.lab403.kopycat.cores.ppc.operands

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



class MSP430Immediate(dtyp: Datatype, value: Long, signed: Boolean) : Immediate<PPCCore>(value, signed, dtyp)