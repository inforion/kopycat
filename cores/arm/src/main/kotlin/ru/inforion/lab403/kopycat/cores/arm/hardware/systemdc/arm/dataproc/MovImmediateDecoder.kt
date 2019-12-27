package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.dataproc

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.arm.ARMExpandImm_C
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.logic.immediate.MOVi
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMImmediate
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by a.gladkikh on 31.01.18
 */
object MovImmediateDecoder {
    class A1(cpu: AARMCore) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val cond = find<Condition> { it.opcode == data[31..28].asInt } ?: Condition.AL
            val rd = ARMRegister.gpr(data[15..12].asInt)
            val (imm32, carry) = ARMExpandImm_C(data[11..0], core.cpu.flags.c.asInt)
            val imm = ARMImmediate(imm32, true)
            val setflags = data[20] == 1L
            return MOVi(core, data, cond, setflags, carry.toBool(), rd, imm, 4)
        }
    }

    class A2(cpu: AARMCore) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val cond = find<Condition> { it.opcode == data[31..28].asInt } ?: Condition.AL
            val rd = ARMRegister.gpr(data[15..12].asInt)
            val imm4 = data[19..16]
            val imm12 = data[11..0]
            val imm = ARMImmediate(cat(imm4, imm12, 11), true)
            if (rd.reg == 15) throw ARMHardwareException.Unpredictable
            return MOVi(core, data, cond, false, false, rd, imm, 4)
        }
    }
}