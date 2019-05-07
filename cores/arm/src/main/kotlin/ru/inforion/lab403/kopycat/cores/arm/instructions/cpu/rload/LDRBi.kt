package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 25.01.18
 */

class LDRBi(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            val index: Boolean,
            val add: Boolean,
            val wback: Boolean,
            val rn: ARMRegister,
            val rt: ARMRegister,
            val imm: Immediate<AARMCore>,
            size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rt, imm, size = size) {

    // A8-419 ARM DDI 0406C.b ID072512

    override val mnem = "LDRB$mcnd"

    override fun execute() {
        val offsetAddress = rn.value(core) + if (add) imm.zext else -imm.zext
        val address = if (index) offsetAddress else rn.value(core)
        rt.value(core, core.inb(address))
        if (wback) rn.value(core, offsetAddress)
    }
}