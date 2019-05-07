package ru.inforion.lab403.kopycat.cores.arm.hardware.processors

import ru.inforion.lab403.kopycat.cores.base.abstracts.ACOP
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by the bat on 13.01.18.
 */

abstract class AARMCOP(core: AARMCore, name: String) : ACOP<AARMCOP, AARMCore>(core, name)
