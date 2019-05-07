package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.logic.register

import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 18.01.18
 */

class MOVr(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val setFlags: Boolean,
           val rd: ARMRegister,
           val rn: ARMRegister,
           val rm: ARMRegister,
           val shiftT: SRType,
           val shiftN: Int,
           size: Int): AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rm, size = size) {
    override val mnem = "MOV${if(setFlags) "S" else ""}$mcnd"
    private var result = ARMVariable(Datatype.DWORD)

    override fun execute() {
        result.value(core, rm)
        if (rd.reg == 15) {
            if(setFlags) throw Unpredictable
            core.cpu.ALUWritePC(result.value(core))
        } else {
            rd.value(core, result)
            if (setFlags) {
                core.cpu.flags.n = result.isNegative(core)
                core.cpu.flags.z = result.isZero(core)
            }
        }
    }
}