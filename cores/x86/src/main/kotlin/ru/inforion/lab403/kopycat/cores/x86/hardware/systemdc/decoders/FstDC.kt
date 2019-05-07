package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.Fst
import ru.inforion.lab403.kopycat.cores.x86.operands.x86FprRegister
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 08.09.16.
 */

class FstDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val currByte = s.peekByte().toInt()
        val column = currByte[5..3]
        val popNumber = if (column == 3) 1 else 0
        val rm = RMDC(s, prefs)
        val op0 = when (opcode) {
            0xD9 -> rm.m32
            0xDB -> TODO("m80real")
            0xDD -> {
                when{
                    currByte in 0xD0 until 0xE0 -> {
                        val subCode = currByte[2..0].toInt()
                        x86FprRegister(subCode)
                    }
                    column in 2..3 -> rm.m64
                    else -> throw GeneralException("Some shit FstDC")
                }
            }
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        val op1 = x86FprRegister(0)
        return Fst(core, s.data, prefs, popNumber, op0, op1)
    }
}