package ru.inforion.lab403.kopycat.cores.v850es.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.v850es.constructor
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esDisplacement
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esImmediate
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 * Created by v.davydov on 03.06.17.
 *
 * lsb - index in opcode of least significant bit (LSB) of 16-bit displacement. If bit == -1 then LSB = 0
 */
class FormatVII(
        core: v850ESCore,
        val construct: constructor, val dtyp: Datatype, val lsb: Int = -1
) : ADecoder<AV850ESInstruction>(core) {

    override fun decode(s: Long): AV850ESInstruction {
        val reg1 = v850esRegister.gpr(s[4..0].asInt)
        val reg2 = v850esRegister.gpr(s[15..11].asInt)
        val bit = if (lsb != -1) s[lsb] else 0
        val offset = cat(s[31..17], bit, 0)
        val imm = v850esImmediate(Datatype.DWORD, signext(offset, 16).asLong, true)
        val disp = v850esDisplacement(dtyp, reg1, imm)
        return construct(core, 4, arrayOf(reg2, disp))
    }
}