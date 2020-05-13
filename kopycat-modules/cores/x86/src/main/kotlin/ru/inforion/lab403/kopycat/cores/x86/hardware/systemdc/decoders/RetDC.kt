package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.branch.Ret
import ru.inforion.lab403.kopycat.cores.x86.operands.zero
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class RetDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val insn = when (opcode) {
            // TODO: Check is16Bit mode conditions
            0xC2 -> Ret(core, s.imm16, s.data, prefs, isFar = false)
            0xC3 -> Ret(core, zero, s.data, prefs, isFar = false)
            0xCA -> Ret(core, s.imm16, s.data, prefs, isFar = true)
            0xCB -> Ret(core, zero, s.data, prefs, isFar = true)
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return insn
    }
}