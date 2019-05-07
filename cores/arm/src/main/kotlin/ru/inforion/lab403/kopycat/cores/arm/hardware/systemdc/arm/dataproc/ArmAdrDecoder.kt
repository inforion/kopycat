package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.dataproc

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.ARMExpandImm
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition.AL
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.special.ADR
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMImmediate
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

object ArmAdrDecoder {
    abstract class Common(cpu: AARMCore, val add: Boolean) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val cond = find<Condition> { it.opcode == data[31..28].asInt } ?: AL
            val rd = ARMRegister.gpr(data[15..12].asInt)
            val imm = ARMImmediate(ARMExpandImm(data[11..0]), true)
            return ADR(core, data, cond, add, rd, imm, 4)
        }
    }

    class A1(cpu: AARMCore) : Common(cpu, true)
    class A2(cpu: AARMCore) : Common(cpu, false)
}