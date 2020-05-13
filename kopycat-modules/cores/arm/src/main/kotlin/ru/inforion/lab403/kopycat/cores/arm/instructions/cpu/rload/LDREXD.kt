package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



// See A8.8.77
class LDREXD(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            val rn: ARMRegister,
            val rt: ARMRegister,
            val rt2: ARMRegister):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rt, rn) {

    override val mnem = "LDREXD$mcnd"

    override fun execute() {
        val address = rn.value(core)
        // LDREXD requires doubleword-aligned address
        if (address[2..0] == 0b000L) throw ARMHardwareException.AligmentFault // TODO: Not implemented - AlignmentFault(address, FALSE)
        // TODO: Single core - no need
        //SetExclusiveMonitors(address,8);
        // See the description of Single-copy atomicity for details of whether
        // the two loads are 64-bit single-copy atomic.
        rt.value(core, core.inl(address))
        rt2.value(core, core.inl(address + 4))
    }
}