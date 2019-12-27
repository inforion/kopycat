package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.shift

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.kopycat.cores.arm.SRType.SRType_ASR
import ru.inforion.lab403.kopycat.cores.arm.Shift_C
import ru.inforion.lab403.kopycat.cores.arm.UInt
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by a.gladkikh on 01.02.18
 */

class ASRr(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           private val setFlags: Boolean,
           val rd: ARMRegister,
           val rn: ARMRegister,
           val rm: ARMRegister,
           size: Int): AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rn, rm, size = size) {
    override val mnem = "ASR${if(setFlags) "S" else ""}$mcnd"
    private var result = ARMVariable(Datatype.DWORD)

    override fun execute() {
        val shiftN = UInt(rm.value(core), rm.dtyp.bits).asInt
        val (res, carry) = Shift_C(rn.value(core), 32, SRType_ASR, shiftN, core.cpu.flags.c.asInt)
        result.value(core, res)
        rd.value(core, result)
        if (setFlags)
            FlagProcessor.processLogicFlag(core, result, carry == 1)
    }
}