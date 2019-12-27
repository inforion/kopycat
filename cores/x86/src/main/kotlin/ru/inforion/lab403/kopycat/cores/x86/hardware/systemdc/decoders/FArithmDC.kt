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
 * Created by v.davydov on 22.09.16.
 *
 * This is universal decoder for all arithmetical fpu operations:
 * FADD,
 * FMUL
 * FCOM,
 * FCOMP
 * FSUB,
 * SDUBR
 * FDIV,
 * FDIVR
 *
 * It is very wired, because it work in 2 modes (for byte 0xD8, 0xDC, 0xDE)
 * and 1 mode for byte 0xDA
 *
 * This is illustration for bytes 0xD8, 0xDC, 0xDE:
 * If next byte is in range [RANGE_RM_MODE] (0x00-0xBF), you must get column (5..3 bits) and
 * get m16/m32/m64 value. If this byte in range 0xC0-0xFF, you must see this table:
 *
 *      0x0  0x1  0x2  0x3  0x4  0x5  0x6  0x7  0x8   0x9  0xA  0xB  0xC  0xD  0xE  0xF
 * 0xC                   FADD                       |                FMUL
 * 0xD                   FCOM                       |                FCOMP
 * 0xE                   FSUB                       |                SDUBR
 * 0xF                   FDIV                       |                FDIVR
 * Then you must get value (i = byte % 0x08) and instruction operands will be ST(i), ST(0)
 */

class FArithmDC(core: x86Core, val construct: (x86Core, ByteArray, Prefixes, Int, Array<AOperand<x86Core>>) -> AX86Instruction) :
        ADecoder<AX86Instruction>(core) {
    private val RANGE_RM_MODE = 0x00..0xBF

    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val currByte = s.peekByte()
        val rm = RMDC(s, prefs)
        var popCount = 0
        val ops = when (opcode) {
            0xD8 -> {
                if(currByte in RANGE_RM_MODE) {
                    arrayOf(x86FprRegister(0), rm.m32)
                } else {
                    val index = currByte[2..0].toInt()
                    s.readByte()
                    arrayOf(x86FprRegister(0), x86FprRegister(index))
                }
            }
            0xDA -> {
                when(currByte[5..3].toInt()){
                    0 -> arrayOf(x86FprRegister(0), rm.m32)
                    else -> throw GeneralException("Incorrect opcode in decoder")
                }
            }
            0xDC -> {
                if(currByte in RANGE_RM_MODE) {
                    arrayOf(x86FprRegister(0), rm.m64)
                } else {
                    val index = currByte[2..0].toInt()
                    s.readByte()
                    arrayOf(x86FprRegister(index), x86FprRegister(0))
                }
            }
            0xDE -> {
                if(currByte in RANGE_RM_MODE) {
                    arrayOf(x86FprRegister(0), rm.m16)
                } else {
                    val index = currByte[2..0].toInt()
                    popCount = 1
                    s.readByte()
                    arrayOf(x86FprRegister(index), x86FprRegister(0))
                }
            }
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return construct(core, s.data, prefs, popCount, ops as Array<AOperand<x86Core>>)
    }
}