package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by the bat on 17.01.18.
 */

class ThumbMulDecoder(
        cpu: AARMCore,
        val constructor: (
                cpu: AARMCore,
                opcode: Long,
                cond: Condition,
                setFlags: Boolean,
                rd: ARMRegister,
                rm: ARMRegister,
                rn: ARMRegister,
                size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val cond = find<Condition> { it.opcode == data[31..28].asInt }?: Condition.AL
        val rn = ARMRegister.gpr(data[5..3].asInt)
        val rd = ARMRegister.gpr(data[2..0].asInt)
        val rm = ARMRegister.gpr(data[2..0].asInt)
        val setFlags = !core.cpu.InITBlock()
        if(core.cpu.ArchVersion() < 6 && rd.reg == rn.reg) throw Unpredictable
        return constructor(core, data, cond, setFlags, rd, rn, rm, 2)
    }
}