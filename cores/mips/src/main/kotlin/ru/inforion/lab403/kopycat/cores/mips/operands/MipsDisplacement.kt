package ru.inforion.lab403.kopycat.cores.mips.operands

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.ANY
import ru.inforion.lab403.kopycat.cores.base.operands.Displacement
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by davydov_vn on 06.10.16.
 */

class MipsDisplacement(dtyp: Datatype, reg: Int, addr: Int) :
        Displacement<MipsCore>(dtyp, GPR(reg), MipsImmediate(addr.asULong), ANY)

