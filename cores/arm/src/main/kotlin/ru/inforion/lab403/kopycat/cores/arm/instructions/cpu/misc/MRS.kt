package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.misc

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 25.01.18
 */

class MRS(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val rd: ARMRegister,
          val readSPSR: Boolean):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd) {
    override val mnem = "MSR$mcnd"

    val result = ARMVariable(Datatype.WORD)
    override fun execute() {
        if(readSPSR)
            if(core.cpu.CurrentModeIsUserOrSystem())
                throw Unpredictable
            else
                rd.value(core, core.cpu.sregs.apsr and core.cpu.sregs.cpsr )
        else
            rd.value(core, core.cpu.sregs.apsr and core.cpu.sregs.cpsr and 0b1111_1000_1111_1111_0000_0011_1101_1111L)
    }
}