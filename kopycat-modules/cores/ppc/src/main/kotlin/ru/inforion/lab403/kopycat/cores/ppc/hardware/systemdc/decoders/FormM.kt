package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



class FormM(core: PPCCore,
            val construct:  (PPCCore, Int, Int, Int, Boolean, Array<AOperand<PPCCore>>) -> APPCInstruction
) : APPCDecoder(core) {

    override fun decode(s: Long): APPCInstruction {

        val rS = PPCRegister.gpr(s[25..21].toInt())
        val rA =  PPCRegister.gpr(s[20..16].toInt())
        val shift = s[15..11].toInt()
        val rB =  PPCRegister.gpr(shift)
        val maskFst = s[10..6].toInt()
        val maskSnd = s[5..1].toInt()
        val record = s[0].toBool()

        return construct(core,
                shift,
                maskFst,
                maskSnd,
                record,
                arrayOf(rS, rA, rB)
        )
    }
}