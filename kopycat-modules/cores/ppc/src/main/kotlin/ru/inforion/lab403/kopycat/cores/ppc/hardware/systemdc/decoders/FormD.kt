package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



class FormD(core: PPCCore,
            val construct:  (PPCCore, Long, Boolean, Long, Array<AOperand<PPCCore>>) -> APPCInstruction
) : APPCDecoder(core) {

    override fun decode(s: Long): APPCInstruction {

        val destOrSource = PPCRegister.gpr(s[25..21].toInt())
        val condRegField = s[25..23]
        val length = s[21].toBool()
        val opA =  PPCRegister.gpr(s[20..16].toInt())
        val data = s[15..0]

        return construct(core,
                condRegField,
                length,
                data,
                arrayOf(destOrSource, opA)
        )
    }
}