package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rstore

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unknown
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class STRHi(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            val index: Boolean,
            val add: Boolean,
            val wback: Boolean,
            val rn: ARMRegister,
            val rt: ARMRegister,
            val imm32: Immediate<AARMCore>,
            size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rt, imm32, size = size) {
    override val mnem = "STRH$mcnd"

    override fun execute() {
        val offsetAddress = rn.value(core) + if (add) imm32.value else -imm32.value
        val address = if (index) offsetAddress else rn.value(core)

        if(core.cpu.UnalignedSupport() || address[0] == 0L)
            core.outw(address like Datatype.DWORD, rt.value(core)[15..0])
        else throw Unknown

        if (wback) rn.value(core, offsetAddress)
    }
}