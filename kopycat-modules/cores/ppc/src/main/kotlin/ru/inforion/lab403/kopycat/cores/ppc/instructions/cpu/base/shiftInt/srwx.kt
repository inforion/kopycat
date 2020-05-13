package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.shiftInt

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.ppc.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCVariable
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Shift right word
class srwx(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "srw${if (flag) "." else ""}"

    val rs = PPCRegister.gpr(fieldA)
    val ra = PPCRegister.gpr(fieldB)
    val rb = PPCRegister.gpr(fieldC)

    val result = PPCVariable(Datatype.DWORD)

    //Keep it simple, stupid
    override fun execute() {
        val n = rb.value(core)[4..0].toInt()

        result.value(core, rs.value(core) shr n)

        ra.value(core, result)

        if (flag)
            FlagProcessor.processCR0(core, result)
    }
}