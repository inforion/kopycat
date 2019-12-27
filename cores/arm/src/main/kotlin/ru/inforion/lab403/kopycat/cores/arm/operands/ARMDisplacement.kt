package ru.inforion.lab403.kopycat.cores.arm.operands

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.ANY
import ru.inforion.lab403.kopycat.cores.base.operands.Displacement
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by a.gladkikh on 13.01.18.
 */

fun ARMDisplacement(dtyp: Datatype, reg: ARMRegister, off: Immediate<AARMCore> = zero, access: AOperand.Access = ANY) =
        Displacement(dtyp, reg, off, access)