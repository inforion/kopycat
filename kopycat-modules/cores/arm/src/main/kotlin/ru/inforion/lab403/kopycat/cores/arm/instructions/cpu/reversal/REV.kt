package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.reversal

import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class REV(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val rd: ARMRegister,
          val rm: ARMRegister,
          size: Int) :
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rm, size = size) {
    override val mnem = "REV$mcnd"

    override fun execute() {
        val value = insert(rm.bits(core, 7..0), 31..24)
                .insert(rm.bits(core, 15..8), 23..16)
                .insert(rm.bits(core, 23..16), 15..8)
                .insert(rm.bits(core, 31..24), 7..0)
        rd.value(core, value)
    }
}