package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.ROR
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class LDRi(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val index: Boolean,
           val add: Boolean,
           val wback: Boolean,
           val rn: ARMRegister,
           val rt: ARMRegister,
           val imm: Immediate<AARMCore>,
           size: Int = 4):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rt, rn, imm, size = size) {
    override val mnem = "LDR$mcnd"

    override fun execute() {
        val offsetAddress = rn.value(core) + if (add) imm.value(core) else -imm.value(core)
        val address = if (index) offsetAddress else rn.value(core)
        val data = core.inl(address like Datatype.DWORD)
        if (wback) rn.value(core, offsetAddress)
        if (rt.reg == core.cpu.regs.pc.reg) {
            if (address[1..0] == 0b00L) {
                core.cpu.LoadWritePC(data)
            } else {
                throw Unpredictable
            }
        } else if (core.cpu.UnalignedSupport() || address[1..0] == 0b00L) {
            rt.value(core, data)
        } else {
            val tmp = ROR(data, 32, 8 * address[1..0].asInt)
            rt.value(core, tmp)
        }
    }
}