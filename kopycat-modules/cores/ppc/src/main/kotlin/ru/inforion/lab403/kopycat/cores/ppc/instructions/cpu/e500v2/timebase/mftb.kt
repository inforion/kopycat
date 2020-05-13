package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.e500v2.timebase

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.procCtrl.mfspr
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Move from time base
//typealias mftb = mfspr
class mftb(core: PPCCore, field: Int, vararg operands: AOperand<PPCCore>) : mfspr(core, field, *operands)