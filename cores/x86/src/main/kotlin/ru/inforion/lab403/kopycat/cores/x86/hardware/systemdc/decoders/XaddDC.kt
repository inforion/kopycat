package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory.Xadd
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by a.gladkikh on 27.06.18.
 */

class XaddDC(dev: x86Core) : ADecoder<AX86Instruction>(dev) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.readByte().toInt()
        val rm = RMDC(s, prefs)
        val ops = when (opcode) {
            0xC0 -> arrayOf(rm.r8, rm.m8)
            0xC1 -> arrayOf(rm.mpref, rm.rpref)
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return Xadd(core, s.data, prefs, ops[0], ops[1])
    }
}