package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.bitwise.Btr
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by the bat on 25.06.17.
 */
class BtrDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.readByte().toInt()
        val rm = RMDC(s, prefs)
        val ops = when (opcode) {
            0xB3 -> arrayOf(rm.mpref, rm.rpref)
            0xBA -> arrayOf(rm.mpref, s.imm8)
            else -> throw GeneralException("Incorrect opcode in decoder Bt")
        }
        return Btr(core, s.data, prefs, *ops)
    }
}