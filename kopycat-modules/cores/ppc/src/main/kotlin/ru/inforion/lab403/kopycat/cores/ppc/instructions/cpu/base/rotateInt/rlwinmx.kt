package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.rotateInt

import ru.inforion.lab403.common.extensions.bitMask
import ru.inforion.lab403.common.extensions.mask
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.instructions.rotl32
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCVariable
import ru.inforion.lab403.kopycat.modules.cores.PPCCore




//Rotate left word immediate then AND with mask
class rlwinmx(core: PPCCore, val shift: Int, val maskFst: Int, val maskSnd: Int, val record: Boolean, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "rlwinm${if (record) "." else ""}"

    private val result = PPCVariable(Datatype.DWORD)

    override fun execute() {
        val r = op1.value(core) rotl32 shift
        //WARNING: In documentation MASK(x, y), where x > y.
        //But in PPC msb is zero. So MASK(MB, ME) switches to MASK(ME, MB)
        val m = when {
            maskFst < maskSnd + 1 -> bitMask((31 - maskFst)..(31 - maskSnd))
            maskFst == maskSnd + 1 -> {
                bitMask(32)
            }
            else -> {
                bitMask((32 - maskSnd)..(32 - maskFst)).inv() mask 32
            }
        }


        result.value(core, r and m)

        op2.value(core, result)

        if (record)
            FlagProcessor.processCR0(core, result)
    }
}