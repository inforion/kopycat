package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.trap

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.instructions.ssext
import ru.inforion.lab403.kopycat.cores.ppc.instructions.usext
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Trap word immediate
class twi(core: PPCCore, val condRegField: Long, val length: Boolean, val data: Long, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "twi"

    override fun execute() {
        val extImm = data.ssext(15)
        val extUImm = data.usext(15)
        val a = op2.ssext(core)
        val ua = op2.value(core)

        val to = op1.value(core)
        if ((to[4].toBool() && (a < extImm))
                || (to[3].toBool() && (a > extImm))
                || (to[2].toBool() && (a == extImm))
                || (to[1].toBool() && (ua < extUImm))
                || (to[0].toBool() && (ua > extUImm)))
            TODO("It's a trap")
        TODO("Isn't fully implemented")
    }
}