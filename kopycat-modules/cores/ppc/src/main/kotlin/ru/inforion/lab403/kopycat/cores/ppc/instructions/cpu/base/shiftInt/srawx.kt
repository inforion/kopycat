package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.shiftInt

import ru.inforion.lab403.common.extensions.bitMask
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.ppc.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCVariable
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Shift right algebraic word
class srawx(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "sraw${if (flag) "." else ""}"

    val rs = PPCRegister.gpr(fieldA)
    val ra = PPCRegister.gpr(fieldB)
    val rb = PPCRegister.gpr(fieldC)

    val result = PPCVariable(Datatype.DWORD)
    val data = PPCVariable(Datatype.DWORD)

    //Keep it simple, stupid
    override fun execute() {
        val n = rb.value(core)[4..0].toInt()
        data.value(core, rs.ssext(core)) //Won't work on 64 bit system
        result.value(core, data.value(core) shr n)

        ra.value(core, result)

        FlagProcessor.processCarryAlgShift(core, data, n)

        if (flag)
            FlagProcessor.processCR0(core, result)
    }
}