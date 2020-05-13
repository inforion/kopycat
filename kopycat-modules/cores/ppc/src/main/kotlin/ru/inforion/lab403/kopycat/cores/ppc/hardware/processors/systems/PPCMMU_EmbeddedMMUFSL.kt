package ru.inforion.lab403.kopycat.cores.ppc.hardware.processors.systems

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.ppc.hardware.processors.APPCMMU
import ru.inforion.lab403.kopycat.cores.ppc.operands.systems.PPCRegister_Embedded
import ru.inforion.lab403.kopycat.cores.ppc.operands.systems.PPCRegister_EmbeddedMMUFSL
import ru.inforion.lab403.kopycat.modules.cores.PPCCore

class PPCMMU_EmbeddedMMUFSL(parent: Module, name: String, tlbs: Int = 4, tlbsize: Int = 64)
    : APPCMMU(parent, name, tlbs, tlbsize) {

    override fun processID(): Array<Long> = arrayOf(
            PPCRegister_Embedded.OEAext.PID0.value(core as PPCCore)[13..0],
            PPCRegister_EmbeddedMMUFSL.OEAext.PID1.value(core as PPCCore)[13..0],
            PPCRegister_EmbeddedMMUFSL.OEAext.PID2.value(core as PPCCore)[13..0]
    )
}