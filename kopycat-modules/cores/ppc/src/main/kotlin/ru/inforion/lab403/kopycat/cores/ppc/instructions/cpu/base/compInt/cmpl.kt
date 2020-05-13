package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.compInt

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.common.extensions.toInt
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Compare logical
class cmpl(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "cmpl"

    val condRegField = fieldA[4..2]
    val length = fieldA[0].toBool()
    val ra = PPCRegister.gpr(fieldB)
    val rb = PPCRegister.gpr(fieldC)

    override fun execute() {
        if (length)
            throw GeneralException("64 bit instruction set isn't implemented")

        val a = ra.value(core)
        val b = rb.value(core)

        val c = when {
            a < b -> 0b1000
            a > b -> 0b0100
            else -> 0b0010
        } or core.cpu.xerBits.SO.toInt()
        //TODO: maybe move to FlagProcessor
        core.cpu.crBits.cr(condRegField).field = c.toLong()
    }
}