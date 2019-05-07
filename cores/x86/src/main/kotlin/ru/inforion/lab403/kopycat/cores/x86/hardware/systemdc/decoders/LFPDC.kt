package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by batman on 12/10/16.
 */
class LFPDC(core: x86Core, val construct: (x86Core, ByteArray, Prefixes, AOperand<x86Core>, AOperand<x86Core>) -> AX86Instruction) :
        ADecoder<AX86Instruction>(core) {

    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        if(s.last == 0x0F)
            s.readByte()
        val rm = RMDC(s, prefs)
        val op1 = rm.rpref
        val op2 = rm.m16x32
        return construct(core, s.data, prefs, op1, op2)
    }
}