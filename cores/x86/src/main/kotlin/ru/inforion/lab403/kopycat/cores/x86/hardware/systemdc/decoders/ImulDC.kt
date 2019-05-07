package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.arith.Imul
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 29.09.16.
 */
class ImulDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val rm = RMDC(s, prefs)
        val ops = when (opcode) {
            0xF6 -> arrayOf(rm.m8)
            0xF7 -> arrayOf(rm.mpref)
            0x6B -> arrayOf(rm.rpref, rm.mpref, s.imm8)
            0x69 -> arrayOf(rm.rpref, rm.mpref, s.imm(prefs))
            0x0F -> {
                val sopcode = s.readByte().toInt()
                when (sopcode) {
                    0xAF -> arrayOf(rm.rpref, rm.mpref)
                    else -> throw GeneralException("Incorrect sopcode = $sopcode")
                }
            }
            else -> throw GeneralException("Incorrect opcode = $opcode")
        }
        return Imul(core, s.data, prefs, *ops)
    }
}