package ru.inforion.lab403.kopycat.cores.v850es.operands

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.ANY
import ru.inforion.lab403.kopycat.cores.base.operands.Memory
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

fun v850esMemory(dtyp: Datatype, addr: Long, access: Access = ANY) = Memory<v850ESCore>(dtyp, DWORD, addr, access)