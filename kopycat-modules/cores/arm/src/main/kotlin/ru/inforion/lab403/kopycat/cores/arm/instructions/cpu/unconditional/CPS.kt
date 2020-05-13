package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.unconditional

import ru.inforion.lab403.common.extensions.clr
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.extensions.set
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.AARMv6Core



// See B9.3.2
class CPS(val cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val enable: Boolean,
          val disable: Boolean,
          val changemode: Boolean,
          val mode: Long,
          val affectA: Boolean,
          val affectI: Boolean,
          val affectF: Boolean):
        AARMInstruction(cpu, Type.VOID, cond, opcode) {
    override val mnem = "CPS"

    override fun execute() {
        if (core !is AARMv6Core)
            throw ARMHardwareException.Undefined

        if (core.cpu.CurrentModeIsNotUser()) {
            var cpsr_val = core.cpu.sregs.cpsr.value
            when {
                enable -> {
                    if (affectA) cpsr_val = cpsr_val clr 8
                    if (affectI) cpsr_val = cpsr_val clr 7
                    if (affectF) cpsr_val = cpsr_val clr 6
                }
                disable -> {
                    if (affectA) cpsr_val = cpsr_val set 8
                    if (affectI) cpsr_val = cpsr_val set 7
                    if (affectF) cpsr_val = cpsr_val set 6
                }
            }
            if (changemode) {
                cpsr_val = cpsr_val.insert(mode, 4..0)
            }
            // CPSRWriteByInstr() checks for illegal mode changes
            core.cpu.CPSRWriteByInstr(cpsr_val, 0b1111, false)
        }

    }
}