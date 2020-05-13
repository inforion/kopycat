package ru.inforion.lab403.kopycat.cores.mips.operands

import ru.inforion.lab403.common.extensions.WRONGI
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.Near
import ru.inforion.lab403.kopycat.modules.cores.MipsCore



class MipsNear(offset: Int) : Near<MipsCore>(offset, Datatype.DWORD, WRONGI)