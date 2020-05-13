package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.unconditional

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



// See A8.8.126
class PldDecoder (cpu: AARMCore,
                  private val constructor: (
                          cpu: AARMCore,
                          opcode: Long,
                          cond: Condition,
                          rn: ARMRegister,
                          imm32: Long,
                          add: Boolean,
                          is_pldw: Boolean) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {

    // Encoding A1
    override fun decode(data: Long): AARMInstruction {
        val rn = GPRBank.Operand(data[19..16].toInt())
        val imm32 = data[11..0]
        val add = data[23].toBool()
        val is_pldw = !data[22].toBool()

        return constructor(core, data, Condition.AL, rn, imm32, add, is_pldw)
    }
}