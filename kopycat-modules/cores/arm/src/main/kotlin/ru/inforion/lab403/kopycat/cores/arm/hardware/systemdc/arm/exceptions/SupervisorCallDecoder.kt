package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.exceptions

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMImmediate
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


 
class SupervisorCallDecoder(cpu: AARMCore,
                            val constructor: (
                                    cpu: AARMCore,
                                    opcode: Long,
                                    cond: Condition,
                                    imm: Immediate<AARMCore>,
                                    size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val cond = find<Condition> { it.opcode == data[31..28].asInt } ?: Condition.AL
        val imm32 = ARMImmediate(data[23..0].asLong, false)
        return constructor(core, data, cond, imm32, 4)
    }
}