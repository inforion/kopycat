package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 25.01.18
 */

class LDRBT(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            private val postIndex: Boolean,
            val add: Boolean,
            val rn: ARMRegister,
            val rt: ARMRegister,
            val offset: AOperand<AARMCore>):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rt, offset) {

    // A8-425 ARM DDI 0406C.b ID072512

    override val mnem = "LDRT$mcnd"

    override fun execute() {
        if (core.cpu.CurrentModeIsHyp()) throw ARMHardwareException.Unpredictable
        val offsetAddress = rn.value(core) + if (add) offset.value(core) else -offset.value(core)
        val address = if (postIndex) rn.value(core) else offsetAddress
        rt.value(core, core.inb(address))
        if (postIndex) rn.value(core, offsetAddress)
    }
}