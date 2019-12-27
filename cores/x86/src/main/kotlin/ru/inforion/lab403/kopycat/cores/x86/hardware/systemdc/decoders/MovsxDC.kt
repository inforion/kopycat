package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders


import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory.Movsx
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 28.09.16.
 */
class MovsxDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val ops = when (opcode) {
            0x0F -> {
                val rm1 = RMDC(s, prefs)
//                val rm2 = RMDC(s, prefs)
                val sopcode = s.readByte().toInt()
                when (sopcode) {
                    0xBE -> arrayOf(rm1.rpref, rm1.m8)
                    0xBF -> arrayOf(rm1.rpref, rm1.m16)
                    else -> throw GeneralException("Incorrect opcode in decoder")
                }
            }
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return Movsx(core, s.data, prefs, *ops)
    }
}