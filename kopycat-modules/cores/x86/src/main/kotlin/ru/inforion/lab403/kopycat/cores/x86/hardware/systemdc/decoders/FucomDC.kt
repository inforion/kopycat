package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.Fucom
import ru.inforion.lab403.kopycat.cores.x86.operands.x86FprRegister
import ru.inforion.lab403.kopycat.modules.cores.x86Core




class FucomDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val currByte = s.readByte()
        val op0 = x86FprRegister(0)
        return when (opcode) {
            0xDA -> Fucom(core, s.data, prefs, 2, op0, x86FprRegister(1))
            0xDD -> {
                val popCount = if (currByte[3] == 1L) 1 else 0
                val regIndex = currByte[2..0].toInt()
                when(regIndex){
                    0 -> throw GeneralException("Incorrect opcode in decoder")
                    else -> Fucom(core, s.data, prefs, popCount, op0, x86FprRegister(regIndex))
                }
            }
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
    }
}