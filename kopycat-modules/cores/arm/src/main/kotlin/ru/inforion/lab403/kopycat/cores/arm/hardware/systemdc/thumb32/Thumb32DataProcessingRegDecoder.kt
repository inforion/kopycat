package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb32

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.DecodeImmShift
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class Thumb32DataProcessingRegDecoder(
        cpu: AARMCore,
        val constructor: (
                cpu: AARMCore,
                opcode: Long,
                cond: Condition,
                flags: Boolean,
                rd: ARMRegister,
                rn: ARMRegister,
                rm: ARMRegister,
                shiftT: SRType,
                shiftN: Int,
                size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val cond = find<Condition> { it.opcode == data[31..28].asInt }?: Condition.AL
        val rd = GPRBank.Operand(data[11..8].asInt)
        val rn = GPRBank.Operand(data[19..16].asInt)
        val rm = GPRBank.Operand(data[3..0].asInt)
        val setFlags = data[20] == 1L
        val type = data[5..4]
        val imm3 = data[14..12]
        val imm2 = data[7..6]
        val (shiftT, shiftN) = DecodeImmShift(type, (imm3 shl 2) + imm2)
        if(rd.reg in 15..13 || rn.reg in 15..13 || rm.reg in 15..13) throw Unpredictable
        return constructor(core, data, cond, setFlags, rd, rn, rm, shiftT, shiftN.asInt, 2)
    }
}