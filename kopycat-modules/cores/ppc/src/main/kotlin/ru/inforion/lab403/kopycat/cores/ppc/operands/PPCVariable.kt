package ru.inforion.lab403.kopycat.cores.ppc.operands

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.modules.cores.PPCCore


class PPCVariable(dtyp: Datatype, default: Long = 0) : Variable<PPCCore>(default, dtyp)