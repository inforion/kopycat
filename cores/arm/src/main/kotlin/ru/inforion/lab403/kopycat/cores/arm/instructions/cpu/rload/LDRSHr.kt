package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.Shift
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unknown
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class LDRSHr(cpu: AARMCore,
             opcode: Long,
             cond: Condition,
             val index: Boolean,
             val add: Boolean,
             val wback: Boolean,
             val rt: ARMRegister,
             val rn: ARMRegister,
             val rm: ARMRegister,
             private val shiftT: SRType,
             private val shiftN: Int,
             size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rt, rm, size = size) {
    override val mnem = "LDRSH$mcnd"

    override fun execute() {
        val offset = Shift(rm.value(core), 32, shiftT, shiftN, core.cpu.flags.c.asInt)
        val offsetAddress = rn.value(core) + if (add) offset else -offset
        val address = if (index) offsetAddress else rn.value(core)

        val data = core.inw(address)

        if (wback) rn.value(core, offsetAddress)
        if(core.cpu.UnalignedSupport() || address[0] == 0L)
            rt.value(core, signext(data, 32).asLong)
        else throw Unknown
    }
}