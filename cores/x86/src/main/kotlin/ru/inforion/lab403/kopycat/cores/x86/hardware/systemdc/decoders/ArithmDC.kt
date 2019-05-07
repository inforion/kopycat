package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.mask
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Immediate
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRBL.al
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 21.09.16.
 */

class ArithmDC(core: x86Core, val construct: (x86Core, ByteArray, Prefixes, Array<AOperand<x86Core>>) -> AX86Instruction) :
        ADecoder<AX86Instruction>(core) {

    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last and 0xC7
        val rm = RMDC(s, prefs)
        val ops = when (opcode) {
            0x00 -> arrayOf(rm.m8, rm.r8)
            0x01 -> arrayOf(rm.mpref, rm.rpref)
            0x02 -> arrayOf(rm.r8, rm.m8)
            0x03 -> arrayOf(rm.rpref, rm.mpref)
            0x04 -> arrayOf(al, s.imm8)
            0x05 -> arrayOf(x86Register.gpr(prefs.opsize, x86GPR.EAX.id), s.imm(prefs))
            0x80 -> arrayOf(rm.m8, s.imm8)
            0x81 -> arrayOf(rm.mpref, s.imm(prefs))
            0x82 -> TODO()
            0x83 -> arrayOf(rm.mpref, x86Immediate(prefs.opsize, s.imm8.ssext(core) mask prefs.opsize.bits))  // TODO! make it pretty
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return construct(core, s.data, prefs, ops)
    }
}