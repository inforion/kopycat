package ru.inforion.lab403.kopycat.cores.x86.operands

import ru.inforion.lab403.common.extensions.WRONGI
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.x86Core

fun x86Immediate(dtyp: Datatype, value: Long) = Immediate<x86Core>(value, false, dtyp, WRONGI)
val zero = x86Immediate(Datatype.DWORD, 0)
val one = x86Immediate(Datatype.DWORD, 1)