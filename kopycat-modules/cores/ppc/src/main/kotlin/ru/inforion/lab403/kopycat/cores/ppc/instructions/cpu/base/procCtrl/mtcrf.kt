package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.procCtrl

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.enums.eCR
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Move to condition register fields
class mtcrf(core: PPCCore, val field: Int, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "mtcrf"

    val fxm = field[1..8]

    override fun execute() {

        val rs = op1.value(core)
        for (i: Int in 0..7)
            if (fxm[i].toBool()) {
                val cr = core.cpu.crBits.cr(i)
                cr.field = rs[eCR.msb(i)..eCR.lsb(i)]   // if fxm[31 - x / 4] { CR[x + 3 .. x] = RS[x + 3 .. x] }
                                                        // don't forget, that PPC counts bits from msb to lsb as from 0 to n
            }
    }
}