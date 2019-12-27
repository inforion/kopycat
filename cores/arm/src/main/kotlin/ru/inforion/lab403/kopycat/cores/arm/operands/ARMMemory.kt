package ru.inforion.lab403.kopycat.cores.arm.operands

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.ANY
import ru.inforion.lab403.kopycat.cores.base.operands.Memory
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by a.gladkikh on 13.01.18.
 */

fun ARMMemory(dtyp: Datatype, addr: Long, access: Access = ANY) = Memory<AARMCore>(dtyp, DWORD, addr, access)