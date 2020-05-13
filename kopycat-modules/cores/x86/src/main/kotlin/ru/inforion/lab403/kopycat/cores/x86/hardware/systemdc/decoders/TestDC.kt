package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.bitwise.Test
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class TestDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val rm = RMDC(s, prefs)
        val ops = when (opcode) {
            0xA8 -> arrayOf(x86Register.gpr8(x86GPR.EAX.id), s.imm8)
            0xA9 -> arrayOf(x86Register.gpr(prefs.opsize, x86GPR.EAX.id), s.imm(prefs))
            0xF6 -> arrayOf(rm.m8, s.imm8)
            0xF7 -> arrayOf(rm.mpref, s.imm(prefs))
            0x84 -> arrayOf(rm.m8, rm.r8)
            0x85 -> arrayOf(rm.mpref, rm.rpref)
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return Test(core, s.data, prefs, *ops)
    }
}