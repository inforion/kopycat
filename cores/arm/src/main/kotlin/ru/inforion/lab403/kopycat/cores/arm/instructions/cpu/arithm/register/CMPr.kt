package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.arithm.register

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.kopycat.cores.arm.AddWithCarry
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.Shift
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 18.01.18
 */

class CMPr(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           var setFlags: Boolean,
           var rd: ARMRegister,
           var rn: ARMRegister,
           var rm: ARMRegister,
           var shiftT: SRType,
           var shiftN: Int,
           size: Int): AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rm, size = size) {

    override val mnem = "CMP$mcnd"

    override fun execute() {
        val shifted = Immediate<AARMCore>(Shift(rm.value(core), rm.dtyp.bits, shiftT, shiftN, core.cpu.flags.c.asInt))
        val (result, carry, overflow) = AddWithCarry(rn.dtyp.bits, rn.value(core), shifted.inv(core), 1)
        FlagProcessor.processArithmFlag(core, result, carry, overflow)
    }
}