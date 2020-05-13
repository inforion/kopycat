package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.logicInt

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toInt
import ru.inforion.lab403.common.extensions.toLong
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.ppc.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Compare bytes
class cmpb(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "cmpb"

    val rs = PPCRegister.gpr(fieldA)
    val ra = PPCRegister.gpr(fieldB)
    val rb = PPCRegister.gpr(fieldC)

    override fun execute() {
        val rsv = rs.value(core)
        val rbv = rb.value(core)
        var rav = 0L
        for (i: Int in 0..3) {
            val byteRng = ((i + 1) * 8 - 1)..(i * 8)
            val res = (rsv[byteRng] == rbv[byteRng]).toLong() shl (i * 8)
            rav = rav or res
        }

        ra.value(core, rav)
    }
}