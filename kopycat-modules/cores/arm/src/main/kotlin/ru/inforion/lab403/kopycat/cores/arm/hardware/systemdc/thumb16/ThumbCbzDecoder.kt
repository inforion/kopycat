package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMImmediate
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class ThumbCbzDecoder(cpu: AARMCore,
                      private val constructor: (
                              cpu: AARMCore,
                              opcode: Long,
                              cond: Condition,
                              rn: ARMRegister,
                              nonZero: Boolean,
                              imm: Immediate<AARMCore>,
                              size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val rn = GPRBank.Operand(data[2..0].asInt)
        val imm5 = data[7..3]
        val i = data[9]
        val imm = ARMImmediate((imm5 shl 1).insert(i, 6), true)
        val nonZero = data[11] == 1L
        if(core.cpu.InITBlock()) throw Unpredictable
        return constructor(core, data, Condition.AL, rn, nonZero, imm, 2)
    }
}