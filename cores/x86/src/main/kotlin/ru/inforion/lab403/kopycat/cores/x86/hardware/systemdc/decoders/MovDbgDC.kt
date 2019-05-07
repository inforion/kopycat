package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory.Mov
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 22.09.16.
 */

class MovDbgDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val ops = when(opcode){
            0x0F -> {
                val sopcode = s.readByte().toInt()
                val raw = s.readByte()
                val dbgid = raw[5..3].toInt()
                val rid = raw[2..0].toInt()
                when (sopcode) {
                    0x21 -> arrayOf(x86Register.gpr(prefs.opsize, rid), x86Register.dbg(dbgid))
                    0x23 -> arrayOf(x86Register.dbg(dbgid), x86Register.gpr(prefs.opsize, rid))
                    else -> throw GeneralException("Incorrect opcode in decoder")
                }
            }
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return Mov(core, s.data, prefs, *ops)
    }
}