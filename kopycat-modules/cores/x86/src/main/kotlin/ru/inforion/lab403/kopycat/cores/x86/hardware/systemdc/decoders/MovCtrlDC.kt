package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory.Mov
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class MovCtrlDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val ops = when(opcode){
            0x0F -> {
                val sopcode = s.readByte().toInt()
                val raw = s.readByte()
                val ctrlid = raw[5..3].toInt()
                if (ctrlid == 1 || ctrlid > 4)
                    throw GeneralException("Can't operate not with CR0, CR2-CR4")
                val rid = raw[2..0].toInt()
                when (sopcode) {
                    0x20 -> arrayOf(x86Register.gpr(prefs.opsize, rid), x86Register.creg(ctrlid))
                    0x22 -> arrayOf(x86Register.creg(ctrlid), x86Register.gpr(prefs.opsize, rid))
                    else -> throw GeneralException("Incorrect opcode in decoder")
                }
            }
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return Mov(core, s.data, prefs, *ops)
    }
}