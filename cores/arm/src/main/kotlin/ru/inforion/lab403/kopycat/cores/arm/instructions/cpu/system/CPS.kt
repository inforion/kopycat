package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.system

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by a.gladkikh on 30.01.18
 */

class CPS(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          private val im: Boolean,
          size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, size = size) {
    override val mnem = "CPS"

    override fun execute() {
        core.cpu.spr.pm = im
    }
}