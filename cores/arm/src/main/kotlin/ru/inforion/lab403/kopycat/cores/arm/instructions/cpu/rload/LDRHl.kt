package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.Align
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unknown
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class LDRHl(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            val add: Boolean,
            val rt: ARMRegister,
            val imm32: Immediate<AARMCore>):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rt, imm32) {
    override val mnem = "LDRH$mcnd"

    override fun execute() {
        val base = Align(core.cpu.pc, 4)
        val address = base + if (add) imm32.value else -imm32.value
        val data = core.inl(address)

        if(core.cpu.UnalignedSupport() || address[0] == 0L)
            rt.value(core, data)
        else throw Unknown
    }
}