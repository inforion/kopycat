package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



class FormI(core: PPCCore,
            val construct:  (PPCCore, Long, Boolean, Boolean) -> APPCInstruction
) : APPCDecoder(core) {

    override fun decode(s: Long): APPCInstruction {
        val address = s[25..2]
        val absolute = s[1].toBool()
        val linkage = s[0].toBool()
        return construct(core,
                address,
                absolute,
                linkage
        )
    }
}