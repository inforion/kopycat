package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.Shift
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by a.gladkikh on 31.01.18
 */

class LDRr(cpu: AARMCore,
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
    override val mnem = "LDR$mcnd"

    override fun execute() {
        val offset = Shift(rm.value(core), 32, shiftT, shiftN, core.cpu.flags.c.asInt)
        val offsetAddress = rn.value(core) + if(add) offset else -offset
        val address = if(index) offsetAddress else rn.value(core)
        val data = core.inl(address)
        if(wback) rn.value(core, offsetAddress)
        if (rt.reg == 15)
            if (address[1..0] == 0b00L) core.cpu.LoadWritePC(data)
            else throw Unpredictable
        else if (core.cpu.UnalignedSupport() || address[1..0] == 0b00L) rt.value(core, data)
        else throw ARMHardwareException.Unknown
    }
}