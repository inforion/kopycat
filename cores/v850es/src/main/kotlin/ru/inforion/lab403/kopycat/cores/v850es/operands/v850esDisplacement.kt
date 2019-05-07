package ru.inforion.lab403.kopycat.cores.v850es.operands

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.ANY
import ru.inforion.lab403.kopycat.cores.base.operands.Displacement
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

fun v850esDisplacement(dtyp: Datatype, reg: v850esRegister, off: Immediate<v850ESCore> = zero, access: AOperand.Access = ANY) =
        Displacement(dtyp, reg, off, access)