package ru.inforion.lab403.kopycat.cores.mips.operands

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by davydov_vn on 06.10.16.
 */
class MipsImmediate(value: Long, dtyp: Datatype = WORD, signed: Boolean = false) : Immediate<MipsCore>(value, signed, dtyp)

