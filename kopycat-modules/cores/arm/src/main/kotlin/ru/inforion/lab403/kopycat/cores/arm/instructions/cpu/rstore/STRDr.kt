package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rstore

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.HaveLPAE
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class STRDr(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            val index: Boolean,
            val add: Boolean,
            val wback: Boolean,
            val rn: ARMRegister,
            val rt: ARMRegister,
            private val rt2: ARMRegister,
            val rm: ARMRegister):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rt, rt2, rm) {
    override val mnem = "STRD$mcnd"

    override fun execute() {
        val offsetAddress = rn.value(core) + if (add) rm.value(core) else -rm.value(core)
        val address = if (index) offsetAddress else rn.value(core)

        if(HaveLPAE() && address[2..0] == 0L)
            throw GeneralException("Not implemented!")
        else {
            core.outl((address + 0) like Datatype.DWORD, rt.value(core))
            core.outl((address + 4) like Datatype.DWORD, rt2.value(core))
        }
        if (wback) rn.value(core, offsetAddress)
    }
}