package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.system.Insw
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Displacement
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.edi
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.di
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.ds
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Created by v.davydov on 29.06.17.
 */
class InswDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val reg = if (prefs.is16BitAddressMode) di else edi
        val ops = when (opcode) {
            0x6D -> arrayOf(x86Displacement(Datatype.WORD, reg, ssr = ds), x86Register.gpr(Datatype.WORD, x86GPR.EDX.id))
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return Insw(core, s.data, prefs, *ops)
    }
}