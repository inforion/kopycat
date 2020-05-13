package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



class FormA(core: PPCCore,
            val construct:  (PPCCore, Int, Int, Int, Int, Boolean) -> APPCInstruction
) : APPCDecoder(core) {

    //Disclamer:
    //Look FormX for details of implementation
    override fun decode(s: Long): APPCInstruction {

        //Bits 25..21 (6..10 in PPC notation)
        val fieldA = s[25..21].toInt()

        //Bits 20..16 (11..15 in PPC notation)
        val fieldB = s[20..16].toInt()

        //Bits 15..11 (16..20 in PPC notation)
        val fieldC = s[15..11].toInt()

        //Bits 10..6 (21..25 in PPC notation)
        val fieldD = s[10..6].toInt()

        val flag = s[0].toBool()
        return construct(core,
                fieldA,
                fieldB,
                fieldC,
                fieldD,
                flag
        )
    }
}