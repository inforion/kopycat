package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.SRType.SRType_LSL
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class ThumbAddT2Decoder(cpu: AARMCore,
                        val constructor: (
                                cpu: AARMCore,
                                opcode: Long,
                                cond: Condition,
                                setFlags: Boolean,
                                rd: ARMRegister,
                                rn: ARMRegister,
                                rm: ARMRegister,
                                shiftN: Int,
                                shiftT: SRType,
                                size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val dn = data[7].asInt
        val n = ((dn shl 3) + data[2..0]).asInt
        val m = data[6..3].asInt
        val rn = ARMRegister.gpr(n)
        val rd = ARMRegister.gpr(n)
        val rm = ARMRegister.gpr(m)
        if (n == 15 && m == 15) throw Unpredictable
        if (n == 15 && core.cpu.InITBlock() && !core.cpu.LastInITBlock()) throw Unpredictable
        return constructor(core, data, Condition.AL, false, rd, rn, rm, 0, SRType_LSL, 2)
    }
}