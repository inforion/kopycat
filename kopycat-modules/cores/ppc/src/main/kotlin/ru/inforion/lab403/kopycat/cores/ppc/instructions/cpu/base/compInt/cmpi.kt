package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.compInt

import ru.inforion.lab403.common.extensions.toInt
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.instructions.ssext
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Compare immediate
class cmpi(core: PPCCore, val condRegField: Long, val length: Boolean, val data: Long, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "cmpi"

    override fun execute() {
        if (length)
            throw GeneralException("64 bit instruction set isn't implemented")

        val a = op2.ssext(core)
        val extImm = data.ssext(15)
        val c = when {
            a < extImm -> 0b1000
            a > extImm -> 0b0100
            else -> 0b0010
        } or core.cpu.xerBits.SO.toInt()
        //TODO: maybe move to FlagProcessor
        core.cpu.crBits.cr(condRegField.toInt()).field = c.toLong()
    }
}