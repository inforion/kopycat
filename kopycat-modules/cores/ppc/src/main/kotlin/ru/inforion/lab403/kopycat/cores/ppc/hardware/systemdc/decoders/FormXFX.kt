package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore




class FormXFX(core: PPCCore,
            val construct:  (PPCCore, Int, Array<AOperand<PPCCore>>) -> APPCInstruction
) : APPCDecoder(core) {

    override fun decode(s: Long): APPCInstruction {

        val rS = PPCRegister.gpr(s[25..21].toInt())
        val field = s[20..11].toInt()

        return construct(core,
                field,
                arrayOf(rS)
        )
    }
}