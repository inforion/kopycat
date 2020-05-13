package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.misc

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



// TODO: collapse with MSR
class MSRsl(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            val rn: ARMRegister,
            val mask: Int,
            val write_spsr: Boolean) :
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn) {
    override val mnem = "MSR$mcnd"

    override fun execute() {
        if (write_spsr)
            core.cpu.SPSRWriteByInstr(rn.value(core), mask)
        else {
            // Does not affect execution state bits other than E
            core.cpu.CPSRWriteByInstr(rn.value(core), mask, false)
            if (core.cpu.sregs.cpsr.m == 0b11010L && core.cpu.sregs.cpsr.j && core.cpu.sregs.cpsr.t)
                throw ARMHardwareException.Unpredictable
        }
    }
}