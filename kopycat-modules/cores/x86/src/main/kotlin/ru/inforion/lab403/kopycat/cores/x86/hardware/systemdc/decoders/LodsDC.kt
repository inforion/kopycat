package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.string.Lods
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Displacement
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.esi
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.si
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class LodsDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val reg = if (prefs.is16BitAddressMode) si else esi
        val ops = when (opcode) {
            0xAC -> arrayOf(x86Register.gpr(Datatype.BYTE, x86GPR.EAX.id), x86Displacement(Datatype.BYTE, reg, ssr = prefs.ssr))
            0xAD -> arrayOf(x86Register.gpr(prefs.opsize, x86GPR.EAX.id), x86Displacement(prefs.opsize, reg, ssr = prefs.ssr))
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
//        prefs.string = StringPrefix.REP
        return Lods(core, s.data, prefs, *ops)
    }
}