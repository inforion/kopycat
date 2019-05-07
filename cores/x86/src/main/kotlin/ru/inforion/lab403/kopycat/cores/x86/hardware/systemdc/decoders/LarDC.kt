package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders


import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.control.Lar
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 19.01.17.
 */

class LarDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        s.readByte()
        val rm = RMDC(s, prefs)
        val ops = arrayOf(rm.rpref, rm.mpref)
        return Lar(core, s.data, prefs, *ops)
    }
}