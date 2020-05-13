package ru.inforion.lab403.kopycat.cores.ppc.enums

import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.e500v2.eSPR_e500v2
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.eSPR_Embedded
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl.eSPR_EmbeddedMMUFSL
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.APPCSystemDecoder
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.PPCDecoderBase
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.PPCStubDecoder
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.systems.PPCDecoder_Embedded
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.systems.PPCDecoder_EmbeddedMMUFSL
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.systems.PPCDecoder_e500v2
import ru.inforion.lab403.kopycat.cores.ppc.operands.SPR
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



enum class eSystem(val decoder: (PPCCore) -> APPCSystemDecoder, val sprs: List<SPR>) {
    //PowerISA default systems
    Base(::PPCDecoderBase, eSPR.toList()),
    Embedded(::PPCDecoder_Embedded, eSPR_Embedded.toList()),
    EmbeddedMMUFSL(::PPCDecoder_EmbeddedMMUFSL, eSPR_EmbeddedMMUFSL.toList()),

    //Core-specific systems
    e500v2(::PPCDecoder_e500v2, eSPR_e500v2.toList())
}