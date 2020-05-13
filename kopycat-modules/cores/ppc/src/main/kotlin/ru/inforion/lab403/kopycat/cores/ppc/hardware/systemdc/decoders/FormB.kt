package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



class FormB(core: PPCCore,
            val construct:  (PPCCore, Long, Int, Long, Boolean, Boolean) -> APPCInstruction
) : APPCDecoder(core) {

    override fun decode(s: Long): APPCInstruction {
        val options = s[25..21]
        val condition = s[20..16].toInt()
        val address = s[15..2]
        val absolute = s[1].toBool()
        val linkage = s[0].toBool()
        return construct(core,
                options,
                condition,
                address,
                absolute,
                linkage
        )
    }
}