package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.systems

import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.APPCSystemDecoder
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders.FormX
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.support.PatternTable
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.embedded.tlbmanage.tlbivax
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.embedded.tlbmanage.tlbwe
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



class PPCDecoder_EmbeddedMMUFSL(core: PPCCore) : APPCSystemDecoder(core) {

    override val name = "PowerPC Embedded MMU/FSL Decoder"

    private val tlbivaxDc = FormX(core, ::tlbivax)
    private val tlbweDc = FormX(core, ::tlbwe)

    override val group31 = PatternTable("Group of opcode 31",
            arrayOf(10..0),
            arrayOf("11000100100" to tlbivaxDc,
                    "11110100100" to tlbweDc))
}