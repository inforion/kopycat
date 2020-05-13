package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.logicInt

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.ppc.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCVariable
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Count leading zeros word
class cntlzwx(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "cntlzw${if (flag) "." else ""}"

    val rs = PPCRegister.gpr(fieldA)
    val ra = PPCRegister.gpr(fieldB)

    val result = PPCVariable(Datatype.DWORD)

    //Make fun, not var
    private fun countLeadingZeroes(rsv: Long): Long {
        for (i: Int in 31 downTo 0)
            if (rsv[i].toBool())
                return 31L - i.toLong()
        return 32L
    }

    override fun execute() {
        val rsv = rs.value(core)
        val n = countLeadingZeroes(rsv)

        result.value(core, n)

        ra.value(core, result)

        if (flag)
            FlagProcessor.processCR0(core, result)
    }
}