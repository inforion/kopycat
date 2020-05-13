package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.systems

import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.APPCSystemDecoder
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders.FormX
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders.FormXFX
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.support.PatternTable
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.e500v2.memBarier.eieio
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.e500v2.timebase.mftb
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



class PPCDecoder_e500v2(core: PPCCore) : APPCSystemDecoder(core) {

    override val name = "PowerPC e500v2 Decoder"

    private val eieioDc = FormX(core, ::eieio)
    private val mftbDc = FormXFX(core, ::mftb)

    override val group31 = PatternTable("Group of opcode 31",
            arrayOf(10..0),
            arrayOf("01011100110" to mftbDc,
                    "11010101100" to eieioDc))
}