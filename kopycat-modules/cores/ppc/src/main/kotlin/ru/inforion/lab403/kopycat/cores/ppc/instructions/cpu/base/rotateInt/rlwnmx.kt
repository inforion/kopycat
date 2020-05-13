package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.rotateInt

import ru.inforion.lab403.common.extensions.bitMask
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.mask
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.instructions.rotl32
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCVariable
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Rotate left word then AND with mask
class rlwnmx(core: PPCCore, val shift: Int, val maskFst: Int, val maskSnd: Int, val record: Boolean, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "rlwnm${if (record) "." else ""}"

    private val result = PPCVariable(Datatype.DWORD)

    override fun execute() {
        val n = op3.value(core)[4..0].toInt()
        val r = op1.value(core) rotl32 n
        //WARNING: In documentation MASK(x, y), where x > y.
        //But in PPC msb is zero. So MASK(MB, ME) switches to MASK(ME, MB)
        val m = when {
            maskFst < maskSnd + 1 -> bitMask((31 - maskFst)..(31 - maskSnd))
            maskFst == maskSnd + 1 -> {
                bitMask(32)
            }
            else -> {
                bitMask((31 - maskSnd)..(31 - maskFst)).inv() mask 32
            }
        }

        result.value(core, r and m)

        op2.value(core, result)

        if (record)
            FlagProcessor.processCR0(core, result)
    }
}