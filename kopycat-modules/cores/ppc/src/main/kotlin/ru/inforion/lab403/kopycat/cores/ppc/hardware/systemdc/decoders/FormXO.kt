package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



class FormXO(core: PPCCore,
              val construct:  (PPCCore, Boolean, Boolean, Array<AOperand<PPCCore>>) -> APPCInstruction
) : APPCDecoder(core) {

    override fun decode(s: Long): APPCInstruction {
        val rT = PPCRegister.gpr(s[25..21].toInt())
        val rA = PPCRegister.gpr(s[20..16].toInt())
        val rB = PPCRegister.gpr(s[15..11].toInt())
        val overflow = s[21].toBool()
        val record = s[0].toBool()

        return construct(core,
                overflow,
                record,
                arrayOf(rT, rA, rB)
        )
    }
}