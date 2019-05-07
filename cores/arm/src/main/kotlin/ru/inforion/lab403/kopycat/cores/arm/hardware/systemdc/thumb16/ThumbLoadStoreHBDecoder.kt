package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition.UN
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMImmediate
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

object ThumbLoadStoreHBDecoder{
    class Half(cpu: AARMCore,
             private val constructor: (
                     cpu: AARMCore,
                     opcode: Long,
                     cond: Condition,
                     index: Boolean,
                     add: Boolean,
                     wback: Boolean,
                     rn: ARMRegister,
                     rt: ARMRegister,
                     imm: Immediate<AARMCore>,
                     size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val rt = ARMRegister.gpr(data[2..0].asInt)
            val rn = ARMRegister.gpr(data[5..3].asInt)
            val imm = ARMImmediate(data[10..6] shl 1, true)
            return constructor(core, data, UN, true, true, false, rn, rt, imm, 2)
        }
    }

    class Byte(cpu: AARMCore,
               private val constructor: (
                       cpu: AARMCore,
                       opcode: Long,
                       cond: Condition,
                       index: Boolean,
                       add: Boolean,
                       wback: Boolean,
                       rn: ARMRegister,
                       rt: ARMRegister,
                       imm: Immediate<AARMCore>,
                       size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val rt = ARMRegister.gpr(data[2..0].asInt)
            val rn = ARMRegister.gpr(data[5..3].asInt)
            val imm = ARMImmediate(data[10..6], true)
            return constructor(core, data, UN, true, true, false, rn, rt, imm, 2)
        }
    }
}