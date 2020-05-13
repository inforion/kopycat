package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.logicInt

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.ppc.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCVariable
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//NOR
class norx(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "nor${if (flag) "." else ""}"

    val rs = PPCRegister.gpr(fieldA)
    val ra = PPCRegister.gpr(fieldB)
    val rb = PPCRegister.gpr(fieldC)

    val result = PPCVariable(Datatype.DWORD)

    override fun execute() {
        result.value(core, (rs.value(core) or rb.value(core)).inv())

        ra.value(core, result)

        if (flag)
            FlagProcessor.processCR0(core, result)
    }
}