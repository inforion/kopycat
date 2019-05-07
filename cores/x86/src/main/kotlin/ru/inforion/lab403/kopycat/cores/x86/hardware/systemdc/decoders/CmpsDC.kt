package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.string.Cmps
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Displacement
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.edi
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.esi
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.di
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.si
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.es
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by davydov_vn on 28.07.17.
 */
class CmpsDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val src = if (prefs.is16BitAddressMode) si else esi
        val dst = if (prefs.is16BitAddressMode) di else edi

        val ops = when (opcode) {
            0xA6 -> arrayOf(
                    x86Displacement(Datatype.BYTE, dst, ssr = es),
                    x86Displacement(Datatype.BYTE, src, ssr = prefs.ssr))
            0xA7 -> arrayOf(
                    x86Displacement(prefs.opsize, dst, ssr = es),
                    x86Displacement(prefs.opsize, src, ssr = prefs.ssr))
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return Cmps(core, s.data, prefs, *ops)
    }
}