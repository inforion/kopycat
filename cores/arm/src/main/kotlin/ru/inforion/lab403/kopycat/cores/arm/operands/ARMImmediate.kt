package ru.inforion.lab403.kopycat.cores.arm.operands

import ru.inforion.lab403.common.extensions.WRONGI
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by a.gladkikh on 13.01.18.
 */

fun ARMImmediate(value: Long, signed: Boolean) = Immediate<AARMCore>(value, signed, DWORD, WRONGI)
val zero = ARMImmediate(0, false)
val one = ARMImmediate(1, false)