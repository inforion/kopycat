package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.thumb

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.kopycat.cores.arm.AddWithCarry
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.Shift
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 18.01.18
 */

class SUBr(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           private val setFlags: Boolean,
           val rd: ARMRegister,
           val rn: ARMRegister,
           val rm: ARMRegister,
           val shiftN: Int,
           val shiftT: SRType,
           size: Int): AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rn, rm, size = size) {
    override val mnem = "SUB${if(setFlags) "S" else ""}$mcnd"

    override fun execute() {
        val shifted = Shift(rm.value(core), 32, shiftT, shiftN, core.cpu.flags.c.asInt)
        val (result, carry, overflow) = AddWithCarry(rn.dtyp.bits, rn.value(core), shifted.inv(), 1)
        if(rd.reg == 15) {
            core.cpu.ALUWritePC(result)
        } else {
            rd.value(core, result)
            if (setFlags)
                FlagProcessor.processArithmFlag(core, result, carry, overflow)
        }
    }
}
