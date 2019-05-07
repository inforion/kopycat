package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rstore

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 25.01.18
 */

class STRBT(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            val postindex: Boolean,
            val add: Boolean,
            val rn: ARMRegister,
            val rt: ARMRegister,
            val offset: AOperand<AARMCore>):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rt, offset) {
    override val mnem = "STRBT$mcnd"

    override fun execute() {
        if (core.cpu.CurrentModeIsHyp()) throw ARMHardwareException.Unpredictable
        val offsetAddress = rn.value(core) + if (add) offset.value(core) else -offset.value(core)
        val address = if (postindex) rn.value(core) else offsetAddress
        core.outb(address, rt.bits(core, 7..0))
        if (postindex) rn.value(core, offsetAddress)
    }
}