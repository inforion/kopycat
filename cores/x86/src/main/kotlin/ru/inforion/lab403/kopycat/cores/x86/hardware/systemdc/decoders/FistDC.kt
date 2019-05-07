package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.Fist
import ru.inforion.lab403.kopycat.cores.x86.operands.x86FprRegister
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 22.09.16.
 */

class FistDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val currByte = s.peekByte()
        val column = currByte[5..3].toInt()
        val rm = RMDC(s, prefs)
        var popCount = 0
        val op0 = when (opcode) {
            0xDB -> when(column){
                2 -> rm.m32
                3 -> {
                    popCount = 1
                    rm.m32
                }
                else -> throw GeneralException("Incorrect opcode in decoder")
            }
            0xDF -> when(column){
                2 -> rm.m16
                3 -> {
                    popCount = 1
                    rm.m16
                }
                7 -> {
                    popCount = 1
                    rm.m64
                }
                else -> throw GeneralException("Incorrect opcode in decoder")
            }
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        val op1 = x86FprRegister(0)
        return Fist(core, s.data, prefs, popCount, op0, op1)
    }
}