package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86FprRegister
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 22.09.16.
 */

class FArithmDC(core: x86Core, val construct: (x86Core, ByteArray, Prefixes, Int, Array<AOperand<x86Core>>) -> AX86Instruction) :
        ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val currByte = s.peekByte()
        val index = currByte[2..0].toInt()
        val column = currByte[5..3].toInt()
        val rm = RMDC(s, prefs)
        var popCount = 0
        val ops = when (opcode) {
            0xD8 -> {
                when(column){
                    0 -> TODO("FADD m32 real")
                    else -> {
                        s.readByte()
                        arrayOf(x86FprRegister(0), x86FprRegister(index))
                    }
                }
            }
            0xDA -> {
                when(currByte[5..3].toInt()){
                    0 -> arrayOf(x86FprRegister(0), rm.m32)
                    else -> throw GeneralException("Incorrect opcode in decoder")
                }
            }
            0xDC -> {
                when(column){
                    0 -> TODO()
                    else -> {
                        s.readByte()
                        arrayOf(x86FprRegister(index), x86FprRegister(0))
                    }
                }
            }
            0xDE -> {
                when {
                    currByte in 0xC0..0xC7 -> {
                        popCount = 1
                        s.readByte()
                        arrayOf(x86FprRegister(index), x86FprRegister(0))
                    }
                    column == 0 -> arrayOf(x86FprRegister(0), rm.m16)
                    else -> throw GeneralException("Incorrect opcode in decoder")
                }
            }
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return construct(core, s.data, prefs, popCount, ops as Array<AOperand<x86Core>>)
    }
}