package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.multiply

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.SInt
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 22.01.18
 */

class MUL(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val flags: Boolean,
          val rd: ARMRegister,
          val rm: ARMRegister,
          val rn: ARMRegister,
          size: Int): AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rn, rm, size = size) {
    override val mnem = "MUL$mcnd"

    val result = ARMVariable(Datatype.DWORD)
    override fun execute() {
        val operand1 = SInt(rn.value(core), 32)
        val operand2 = SInt(rm.value(core), 32)
        val value = operand1 * operand2
        result.value(core, value[31..0])
        rd.value(core, result)
        if (flags) FlagProcessor.processMulFlag(core, result)
    }
}