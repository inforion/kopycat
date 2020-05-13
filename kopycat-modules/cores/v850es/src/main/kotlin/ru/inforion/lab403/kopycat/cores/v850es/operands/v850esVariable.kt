package ru.inforion.lab403.kopycat.cores.v850es.operands

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore


fun v850esVariable(dtyp: Datatype, default: Long = 0) = Variable<v850ESCore>(default, dtyp)