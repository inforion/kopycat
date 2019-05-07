package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.mips.enums.Designation
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.ProcType
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by ra on 03.06.2016.
 *
 * LB, LBU, LDC1(rt=ft), LDC2, LH, LHU, LW, LWC1(rt=ft), LWC2, LWL, LWR, LL, SB, SC,
 * SDC1(rt=ft), SDC2, SH, SW, SWC1(rt=ft), SWC2, SWL, SWR
 */

class FtOffset(
        core: MipsCore,
        val construct: (MipsCore, Long, MipsRegister<*>, MipsDisplacement) -> AMipsInstruction,
        val dtyp: Datatype,
        val store: AccessAction,
        val type: ProcType
) : ADecoder(core) {

    override fun decode(data: Long): AMipsInstruction {
        val rt = data[20..16].toInt()
        val offset = signext(data[15..0], n = 16)
        val base = data[25..21].toInt()
        return construct(core, data,
                MipsRegister.any(type, Designation.General, rt, 0),
                MipsDisplacement(dtyp, base, offset))
    }
}