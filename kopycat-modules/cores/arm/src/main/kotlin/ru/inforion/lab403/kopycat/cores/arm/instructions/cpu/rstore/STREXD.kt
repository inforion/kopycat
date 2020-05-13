package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rstore

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



// See A8.8.214
class STREXD(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            val rn: ARMRegister,
            val rd: ARMRegister,
            val rt: ARMRegister,
            val rt2: ARMRegister):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rt, rn) {

    override val mnem = "STREXD$mcnd"

    override fun execute() {
        val address = rn.value(core)
        // For the alignment requirements see “Aborts and alignment”
        // Create doubleword to store such that R[t] will be stored at address and R[t2] at address+4.
        val value = if (core.cpu.BigEndian())
            (rt.value(core) shl 32) or rt2.value(core)
        else
            (rt2.value(core) shl 32) or rt.value(core)
        // TODO: Single core - no need
        if (/*ExclusiveMonitorsPass(address, 8)*/ true) {
            core.outl(address, value)
            rd.value(core,0L)
        }
        else
            rd.value(core, 1L)
    }
}