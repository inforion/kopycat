package ru.inforion.lab403.kopycat.cores.ppc.hardware.processors.systems

import ru.inforion.lab403.kopycat.cores.ppc.enums.eSystem
import ru.inforion.lab403.kopycat.cores.ppc.hardware.processors.PPCCPU
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



fun PPCCPU_e500v2(core: PPCCore, name: String) = PPCCPU(core, name, eSystem.Base, eSystem.Embedded, eSystem.EmbeddedMMUFSL, eSystem.e500v2)
fun PPCCPU_Embedded(core: PPCCore, name: String) = PPCCPU(core, name, eSystem.Base, eSystem.Embedded)