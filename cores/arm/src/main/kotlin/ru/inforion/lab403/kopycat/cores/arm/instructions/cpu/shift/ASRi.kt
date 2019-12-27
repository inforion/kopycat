package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.shift

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.kopycat.cores.arm.SRType.SRType_ASR
import ru.inforion.lab403.kopycat.cores.arm.Shift_C
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by a.gladkikh on 01.02.18
 */

class ASRi(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           private val setFlags: Boolean,
           val rd: ARMRegister,
           val rm: ARMRegister,
           imm5: Immediate<AARMCore>,
           val shiftN: Int,
           size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rm, imm5, size = size) {

    override val mnem = "ASR${if(setFlags) "S" else ""}$mcnd"

    private var result = ARMVariable(Datatype.DWORD)

    override fun execute() {
        val (res, carry) = Shift_C(rm.value(core), 32, SRType_ASR, shiftN, core.cpu.flags.c.asInt)
        result.value(core, res)
        if (rd.reg == 15) core.cpu.ALUWritePC(result.value(core))
        else {
            rd.value(core, result)
            if (setFlags)
                FlagProcessor.processLogicFlag(core, result, carry == 1)
        }
    }
}