package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.arithm.immediate

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.kopycat.cores.arm.AddWithCarry
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 18.01.18
 */

class RSCi(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           var setFlags: Boolean,
           var rd: ARMRegister,
           var rn: ARMRegister,
           var imm32: Immediate<AARMCore>,
           size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rn, imm32, size = size) {

    override val mnem = "RSC${if(setFlags) "S" else ""}$mcnd"

    override fun execute() {
        val (result, carry, overflow) = AddWithCarry(rn.dtyp.bits, rn.inv(core), imm32.value, core.cpu.flags.c.asInt)
        if(rd.reg == 15) {
            if(setFlags) throw Unpredictable
            core.cpu.ALUWritePC(result)
        } else {
            rd.value(core, result)
            if (setFlags)
                FlagProcessor.processArithmFlag(core, result, carry, overflow)
        }
    }
}