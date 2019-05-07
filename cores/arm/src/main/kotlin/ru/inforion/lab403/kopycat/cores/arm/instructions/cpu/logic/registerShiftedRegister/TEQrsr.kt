package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.logic.registerShiftedRegister

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.SRType
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
 * Created by the bat on 17.01.18.
 */

class TEQrsr(cpu: AARMCore,
             opcode: Long,
             cond: Condition,
             val setFlags: Boolean,
             val rd: ARMRegister,
             val rn: ARMRegister,
             val rm: ARMRegister,
             val rs: ARMRegister,
             val shiftT: SRType):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rn) {

    override val mnem = "TEQ$mcnd"

    private var result = ARMVariable(Datatype.DWORD)

    override fun execute() {
        val shiftN = rs.value(core)[7..0].asInt
        val (shifted, carry) = Shift_C(rm.value(core), rm.dtyp.bits, shiftT, shiftN, core.cpu.flags.c.asInt)
        result.xor(core, rn, Immediate(shifted))
        FlagProcessor.processLogicFlag(core, result, carry == 1)
    }
}