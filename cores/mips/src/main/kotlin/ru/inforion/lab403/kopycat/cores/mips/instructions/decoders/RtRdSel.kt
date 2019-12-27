package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.xbits
import ru.inforion.lab403.kopycat.cores.mips.enums.Designation
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.ProcType
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by a.gladkikh on 03.06.2016.
 *
 * MFC0, MFC1, MFHC1, CFC1, CFC2
 */

class RtRdSel(
        core: MipsCore,
        val construct: (MipsCore, Long, GPR, MipsRegister<*>) -> AMipsInstruction,
        val type: ProcType,
        val rtyp: Designation
) : ADecoder(core) {

    override fun decode(data: Long): AMipsInstruction {
        val rt = data.xbits(20, 16).toInt()
        val rd = data.xbits(15, 11).toInt()
        // Get 10 bit for selector for common case of MFC0, MFC1, etc.
        val sel = data.xbits(10, 0).toInt()
        return construct(core, data,
                GPR(rt),
                MipsRegister.any(type, rtyp, rd, sel))
    }
}