package ru.inforion.lab403.kopycat.cores.v850es.operands

import ru.inforion.lab403.common.extensions.WRONGI
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

fun v850esImmediate(dtyp: Datatype, value: Long, signed: Boolean) = Immediate<v850ESCore>(value, signed, dtyp, WRONGI)
val zero = v850esImmediate(DWORD, 0, false)
val one = v850esImmediate(DWORD, 1, false)