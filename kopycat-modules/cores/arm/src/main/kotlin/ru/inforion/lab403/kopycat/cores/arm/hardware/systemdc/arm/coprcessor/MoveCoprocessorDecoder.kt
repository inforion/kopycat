package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.coprcessor

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class MoveCoprocessorDecoder(
        cpu: AARMCore,
        val constructor: (
                cpu: AARMCore,
                opcode: Long,
                cond: Condition,
                rd: ARMRegister,
                opcode_1: Int,
                crn: Int,
                cp_num:  Int,
                opcode_2:  Int,
                crm: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val cond = find<Condition> { it.opcode == data[31..28].asInt }?: Condition.AL
        val opcode_1 = data[23..21].toInt()
        val crn = data[19..16].toInt()
        val rd = GPRBank.Operand(data[15..12].asInt)
        val cp_num = data[11..8].toInt()
        val opcode_2 = data[7..5].toInt()
        val crm = data[3..0].toInt()
        return constructor(core, data, cond, rd, opcode_1, crn, cp_num, opcode_2, crm)
    }
}