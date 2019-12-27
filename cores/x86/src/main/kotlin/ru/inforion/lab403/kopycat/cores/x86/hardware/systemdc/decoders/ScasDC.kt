package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.string.Scas
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Displacement
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRBL.al
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.eax
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.edi
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.ax
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.di
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.es
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 26.12.16.
 */

class ScasDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val reg = if (prefs.is16BitAddressMode) di else edi
        val ops = when (opcode) {
            0xAE -> arrayOf(al, x86Displacement(Datatype.BYTE, reg, ssr = es))
            0xAF -> {
                val src = if (prefs.is16BitOperandMode) ax else eax
                arrayOf(src, x86Displacement(prefs.opsize, reg, ssr = es))
            }
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return Scas(core, s.data, prefs, *ops)
    }
}