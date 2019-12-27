package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.Fld
import ru.inforion.lab403.kopycat.cores.x86.operands.x86FprRegister
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 22.09.16.
 */

class FldDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val currByte = s.peekByte().toInt()
        val rm = RMDC(s, prefs)
        val op1 = when (opcode) {
            0xD9 -> {
                val column = currByte[5..3]
                when {
                    currByte in 0xC0..0xC7 -> {
                        val subCode = currByte[2..0]
                        x86FprRegister(subCode) // FLD ST(i)
                    }
                    column == 0 -> rm.m32
                    else -> throw GeneralException("Incorrect opcode in decoder")
                }
            }
            0xDB -> TODO("m80real")
            0xDD -> rm.m64
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        val op0 = x86FprRegister(-1)
        return Fld(core, s.data, prefs, op0, op1)
    }
}