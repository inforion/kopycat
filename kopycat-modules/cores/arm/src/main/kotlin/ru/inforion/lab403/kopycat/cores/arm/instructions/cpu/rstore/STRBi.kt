package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rstore

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class STRBi(cpu: AARMCore,
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
    override val mnem = "STRB$mcnd"

    override fun execute() {
        val offsetAddress = rn.value(core) + if (add) imm.value(core) else -imm.value(core)
        val address = if (index) offsetAddress else rn.value(core)
        core.outb(address like Datatype.DWORD, rt.bits(core, 7..0))
        if (wback) rn.value(core, offsetAddress)
    }
}