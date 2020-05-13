package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.misc

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class CLZ(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val rd: ARMRegister,
          val rm: ARMRegister):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd) {
    override val mnem = "CLZ$mcnd"

    val result = ARMVariable(Datatype.WORD)
    override fun execute() {
        var tmp = 32L
        if (rm.value(core) == 0L) {
            rd.value(core, 32L)
        }
        else {
            for (k in 31 downTo 0) {
                if (rm.value(core)[k] == 1L) {
                    tmp = 31L - k
                    break
                }
            }
            rd.value(core, tmp)
        }
    }
}