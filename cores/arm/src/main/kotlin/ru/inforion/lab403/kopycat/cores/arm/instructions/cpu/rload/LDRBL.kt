package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload

import ru.inforion.lab403.kopycat.cores.arm.Align
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister.GPR.PC
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by r.valitov on 25.01.18
 */

class LDRBL(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            val add: Boolean,
            val rt: ARMRegister,
            val imm: Immediate<AARMCore>,
            size: Int): AARMInstruction(cpu, Type.VOID, cond, opcode, rt, imm, size = size) {
    override val mnem = "LDRB$mcnd"

    // A8-421 ARM DDI 0406C.b ID072512
    override fun execute() {
        val base = Align(PC.value(core), 4)
        val address = base + if (add) imm.zext else -imm.zext
        rt.value(core, core.inb(address))
    }
}