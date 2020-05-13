package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.ppc.enums.eSystem
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.support.InstructionTable
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



class PPCSystemDecoder(core: PPCCore, vararg systems: eSystem) : APPCSystemDecoder(core) {

    override val name: String = "PowerPC System Decoder"

    override val baseOpcode = InstructionTable(
            8, 8,
            { data: Long -> data[31..29] },
            { data: Long -> data[28..26] },
            /////               0,0,0       0,0,1       0,1,0       0,1,1       1,0,0       1,0,1       1,1,0       1,1,1
            /*0,0,0*/  null,       null,       null,       null,       null,       null,       null,       null,
            /*1,0,0*/           null,       null,       null,       null,       null,       null,       null,       null,
            /*0,1,0*/           null,       null,       null,       group13,    null,       null,       null,       null,
            /*1,1,0*/           null,       null,       null,       null,       null,       null,       null,       group31,
            /*0,0,1*/           null,       null,       null,       null,       null,       null,       null,       null,
            /*1,0,1*/           null,       null,       null,       null,       null,       null,       null,       null,
            /*0,1,1*/           null,       null,       null,       null,       null,       null,       null,       null,
            /*1,1,1*/           null,       null,       null,       null,       null,       null,       null,       null
    )

    init {
        for (s in systems) {
            val decoder = s.decoder(core)
            baseOpcode += decoder.baseOpcode
            group13 += decoder.group13
            group31 += decoder.group31
        }
    }
}

