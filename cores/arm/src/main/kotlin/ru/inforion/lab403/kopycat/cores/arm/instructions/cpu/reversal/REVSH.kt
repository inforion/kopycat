package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.reversal

import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 30.01.18
 */

class REVSH(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            val rd: ARMRegister,
            val rm: ARMRegister,
            size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rm, size = size) {
    override val mnem = "REVSH$mcnd"

    override fun execute() {
        rd.bits(core, 31..8, signext(rm.bits(core, 7..0), 8).asLong)
        rd.bits(core, 7..0, rm.bits(core, 15..8))
    }
}