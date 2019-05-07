package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.logic.immediate

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by the bat on 01.02.18
 */

class MOVi(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val setFlags: Boolean,
           val carry: Boolean,
           val rd: ARMRegister,
           val imm32: Immediate<AARMCore>,
           size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, imm32, size = size) {

    override val mnem = "MOV${if(setFlags) "S" else ""}$mcnd"

    val result = ARMVariable(DWORD)

    override fun execute() {
        result.value(core, imm32.value(core))
        if (rd.reg == 15) {
            if(setFlags) throw Unpredictable
            core.cpu.ALUWritePC(result.value(core))
        } else {
            rd.value(core, result)
            if (setFlags)
                FlagProcessor.processLogicFlag(core, result, carry)
        }
    }
}