package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.hint

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 25.01.18
 */

class DBG(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, size = size) {
    override val mnem = "DBG$mcnd"

    override fun execute() = throw GeneralException("Not implemented!")
}