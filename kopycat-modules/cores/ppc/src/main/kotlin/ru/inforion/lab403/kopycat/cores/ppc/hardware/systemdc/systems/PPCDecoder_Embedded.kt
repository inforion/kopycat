package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.systems

import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.APPCSystemDecoder
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders.FormX
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders.FormXL
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.support.PatternTable
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.embedded.procCtrl.mtmsr
import ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.embedded.sysLink.rfi
import ru.inforion.lab403.kopycat.modules.cores.PPCCore




class PPCDecoder_Embedded(core: PPCCore) : APPCSystemDecoder(core) {

    override val name = "PowerPC Embedded Decoder"

    private val mtmsrDc = FormX(core, ::mtmsr)

    private val rfiDc = FormXL(core, ::rfi)

    //Embeded
    /*
    private val dcbiDc = FormX(core, ::dcbi)
    private val mcrxrDc = FormX(core, ::mcrxr)
    */

    override val group13 = PatternTable("Group of opcode 13",
            arrayOf(10..0),
            arrayOf("00001100100" to rfiDc))

    override val group31 = PatternTable("Group of opcode 31",
            arrayOf(10..0),
            arrayOf("00100100100" to mtmsrDc))
    //"01110101100" to dcbiDc,        //E
    //"10000000000" to mcrxrDc,       //E

}