package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.system.Out
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRBL.al
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.dx
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 03.10.16.
 */
class OutDC(core: x86Core) : ADecoder<AX86Instruction>(core) {

    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val ops = when (opcode) {
            0xE6 -> arrayOf(s.imm8, al)
            0xE7 -> arrayOf(s.imm8, x86Register.gpr(prefs.opsize, x86GPR.EAX.id))
            0xEE -> arrayOf(dx, al)
            0xEF -> arrayOf(dx, x86Register.gpr(prefs.opsize, x86GPR.EAX.id))
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return Out(core, s.data, prefs, *ops)
    }
}