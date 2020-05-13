package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload

import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.arm.Align
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class LDRSBl(cpu: AARMCore,
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
        rt.value(core, signext(core.inb(address like Datatype.DWORD), 8).asLong)
    }
}