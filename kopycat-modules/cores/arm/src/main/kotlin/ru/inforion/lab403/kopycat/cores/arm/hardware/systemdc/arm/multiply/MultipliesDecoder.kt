package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.multiply

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class MultipliesDecoder(
        cpu: AARMCore,
        val isSubtract: Boolean,
        val constructor: (
                cpu: AARMCore,
                opcode: Long,
                cond: Condition,
                flags: Boolean,
                rd: ARMRegister,
                ra: ARMRegister,
                rm: ARMRegister,
                rn: ARMRegister) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {

    override fun decode(data: Long): AARMInstruction {
        val cond = find<Condition> { it.opcode == data[31..28].asInt }?: Condition.AL
        val rd = GPRBank.Operand(data[19..16].asInt)
        val ra = GPRBank.Operand(data[15..12].asInt)
        val rm = GPRBank.Operand(data[11..8].asInt)
        val rn = GPRBank.Operand(data[3..0].asInt)
        val setflags = data[20] == 1L

        val pc = core.cpu.regs.pc.reg

        if(rd.reg == pc || rn.reg == pc || rm.reg == pc || ra.reg == pc) throw Unpredictable
        if(!isSubtract && core.cpu.ArchVersion() < 6 && rd.reg == rn.reg) throw Unpredictable

        return constructor(core, data, cond, setflags, rd, ra, rm, rn)
    }
}