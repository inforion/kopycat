package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload

import ru.inforion.lab403.kopycat.cores.arm.Align
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



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
        val base = Align(core.cpu.pc, 4)
        val address = base + if (add) imm.zext else -imm.zext
        rt.value(core, core.inb(address like Datatype.DWORD))
    }
}