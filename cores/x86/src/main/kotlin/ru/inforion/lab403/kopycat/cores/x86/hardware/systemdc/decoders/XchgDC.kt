package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory.Xchg
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 27.12.16.
 */

class XchgDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val rm = RMDC(s, prefs)
        val ops = when (opcode) {
            0x86 -> arrayOf(rm.r8, rm.m8)
            0x87 -> arrayOf(rm.mpref, rm.rpref)
            0x91, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97 -> arrayOf(
                    x86Register.gpr(prefs.opsize, 0),
                    x86Register.gpr(prefs.opsize, opcode % 0x90))
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return Xchg(core, s.data, prefs, *ops)
    }
}