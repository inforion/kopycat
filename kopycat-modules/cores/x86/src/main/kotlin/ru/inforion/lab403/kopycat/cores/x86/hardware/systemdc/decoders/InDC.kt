package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.system.In
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRBL.al
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.dx
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class InDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val ops = when (opcode) {
            0xE4 -> arrayOf(al, s.imm8)
            0xE5 -> arrayOf(x86Register.gpr(prefs.opsize, x86GPR.EAX.id), s.imm8)
            0xEC -> arrayOf(al, dx)
            0xED -> arrayOf(x86Register.gpr(prefs.opsize, x86GPR.EAX.id), dx)
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return In(core, s.data, prefs, *ops)
    }
}