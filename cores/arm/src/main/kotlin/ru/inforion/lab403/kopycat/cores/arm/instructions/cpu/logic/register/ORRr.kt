package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.logic.register

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.Shift_C
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by a.gladkikh on 17.01.18.
 */

class ORRr(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val setFlags: Boolean,
           val rd: ARMRegister,
           val rn: ARMRegister,
           val rm: ARMRegister,
           val shiftT: SRType,
           val shiftN: Int,
           size: Int): AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rm, size = size) {
    override val mnem = "ORR${if(setFlags) "S" else ""}${if(core.cpu.CurrentInstrSet() == AARMCore.InstructionSet.ARM) mcnd else ""}"
    private var result = ARMVariable(Datatype.DWORD)

    override fun execute() {
        val (shifted, carry) = Shift_C(rm.value(core), rm.dtyp.bits, shiftT, shiftN, core.cpu.flags.c.asInt)
        result.or(core, rn, Immediate(shifted))
        if(rd.reg == 15) {
            if(setFlags) throw Unpredictable
            core.cpu.ALUWritePC(result.value(core))
        } else {
            rd.value(core, result)
            if (setFlags)
                FlagProcessor.processLogicFlag(core, result, carry == 1)
        }
    }
}