package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory.Lea
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 28.09.16.
 */
class LeaDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val rm = RMDC(s, prefs)
        val ops = when (opcode) {
            0x8D -> arrayOf(rm.rpref, rm.mpref)
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return Lea(core, s.data, prefs, *ops)
    }
}