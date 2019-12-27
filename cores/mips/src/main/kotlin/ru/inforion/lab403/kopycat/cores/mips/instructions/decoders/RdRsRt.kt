package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by r.aristov on 03.06.2016.
 *
 * ADD, ADDU, AND, CLO, CLZ, DIV(rd=0), DIVU(rd=0), MADD(rd=0), MADDU(rd=0), MSUB, MSUBU,
 * MUL(rd=0), MULT(rd=0), MULTU(rd=0), NOR, OR, SLLV, SLT, SLTU, SUB, SUBU, XOR, MOVN, MOVZ
 */
class RdRsRt(
        core: MipsCore,
        val construct: (MipsCore, Long, GPR, GPR, GPR) -> AMipsInstruction
) : ADecoder(core) {

    override fun decode(data: Long): AMipsInstruction {
        return construct(core, data,
                GPR(data[15..11].toInt()),
                GPR(data[25..21].toInt()),
                GPR(data[20..16].toInt()))
    }
}