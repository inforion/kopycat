package ru.inforion.lab403.kopycat.cores.mips.operands

import ru.inforion.lab403.common.extensions.WRONGI
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.Near
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 23.05.17.
 */

class MipsNear(offset: Int) : Near<MipsCore>(offset, Datatype.DWORD, WRONGI)