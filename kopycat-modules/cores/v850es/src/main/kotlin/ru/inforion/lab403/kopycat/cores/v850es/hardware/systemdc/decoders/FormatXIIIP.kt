package ru.inforion.lab403.kopycat.cores.v850es.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.v850es.constructor
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esImmediate
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



class FormatXIIIP(core: v850ESCore, val construct: constructor) : ADecoder<AV850ESInstruction>(core) {

    override fun decode(s: Long): AV850ESInstruction {
        var size = 4
        val ff = s[20..19].toInt()
        val isLong = s[17]
        val imm = v850esImmediate(DWORD, s[5..1], false)
        val list = v850esImmediate(DWORD, s[0].insert(s[31..21], 11..1), false)

        if (isLong == 0L)
            return construct(core, size, arrayOf(imm, list))

        val epValue = when (ff) {
            0x00 -> {
                size = 4
                v850esRegister.GPR.r3
            }
            0x01 -> {
                size = 6
                v850esImmediate(DWORD, signext(s[47..32], 16).asLong, true)
            }
            0x02 -> {
                size = 6
                v850esImmediate(DWORD, s[47..32] shl 16, false)
            }
            0x03 -> {
                size = 8
                v850esImmediate(DWORD, s[63..32], false)
            }
            else -> throw GeneralException("Incorrect value")
        }
        return construct(core, size, arrayOf(imm, list, epValue))
    }
}