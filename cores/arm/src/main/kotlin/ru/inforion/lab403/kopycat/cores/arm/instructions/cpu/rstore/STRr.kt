package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rstore

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.Shift
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.AARMCore.InstructionSet.ARM

/**
 * Created by r.valitov on 25.01.18
 */

class STRr(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val index: Boolean,
           val add: Boolean,
           val wback: Boolean,
           val rt: ARMRegister,
           val rn: ARMRegister,
           val rm: ARMRegister,
           val shiftT: SRType,
           val shiftN: Int,
           size: Int): AARMInstruction(cpu, Type.VOID, cond, opcode, rt, rn, rm, size = size) {
    override val mnem = "STR$mcnd"

    override fun execute() {
        val offset = Shift(rm.value(core), 32, shiftT, shiftN, core.cpu.flags.c.asInt)
        val offsetAddress = rn.value(core) + if (add) offset else -offset
        val address = if (index) offsetAddress else rn.value(core)
        val data = if (rt.reg == 15) core.cpu.PCStoreValue() else rt.value(core)
        if (core.cpu.UnalignedSupport() || address[1..0] == 0b00L || core.cpu.CurrentInstrSet() == ARM)
            core.outl(address, data)
        else throw ARMHardwareException.Unknown
        if (wback) rn.value(core, offset)
    }
}