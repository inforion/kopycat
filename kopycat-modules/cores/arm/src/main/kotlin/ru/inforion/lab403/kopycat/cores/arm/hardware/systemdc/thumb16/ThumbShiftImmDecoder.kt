package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.DecodeImmShift
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.enums.ShiftType
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class ThumbShiftImmDecoder(
        cpu: AARMCore,
        val type: ShiftType,
        val constructor: (
                cpu: AARMCore,
                opcode: Long,
                cond: Condition,
                flags: Boolean,
                rd: ARMRegister,
                rm: ARMRegister,
                imm5: Immediate<AARMCore>,
                shiftN: Int,
                size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val rd = GPRBank.Operand(data[2..0].asInt)
        val rm = GPRBank.Operand(data[5..3].asInt)
        val imm5 = Immediate<AARMCore>(data[10..6])
        val setFlag = !core.cpu.InITBlock()
        val (_, shiftN) = DecodeImmShift(type.id, imm5.value(core))

        return constructor(core, data, Condition.AL, setFlag, rd, rm, imm5, shiftN.asInt, 2)
    }
}