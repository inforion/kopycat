package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.SRType.SRType_LSL
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class ThumbArithmRegDecoder(cpu: AARMCore,
                            private val constructor: (
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
        val rd = ARMRegister.gpr(data[2..0].asInt)
        val rn = ARMRegister.gpr(data[5..3].asInt)
        val rm = ARMRegister.gpr(data[8..6].asInt)
        val setFlag = !core.cpu.InITBlock()
        return constructor(core, data, Condition.AL, setFlag, rd, rn, rm, 0, SRType_LSL, 2)
    }
}