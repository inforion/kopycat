package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.system

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



// See B9.3.11
// TODO: collapse with MSRsl
class MSR(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val imm32: Immediate<AARMCore>,
          val mask: Int,
          val write_spr: Boolean):
        AARMInstruction(cpu, Type.VOID, cond, opcode, imm32) {
    override val mnem = "MSR$mcnd"

    override fun execute() {
        if (write_spr)
            core.cpu.SPSRWriteByInstr(imm32.value, mask)
        else {
            // Does not affect execution state bits other than E
            core.cpu.CPSRWriteByInstr(imm32.value, mask, false)
            if (core.cpu.sregs.cpsr.m == 0b11010L && core.cpu.sregs.cpsr.j && core.cpu.sregs.cpsr.t)
                throw ARMHardwareException.Unpredictable
        }
    }
}