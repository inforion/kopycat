package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.Shift
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class LDRSBr(cpu: AARMCore,
             opcode: Long,
             cond: Condition,
             val index: Boolean,
             val add: Boolean,
             val wback: Boolean,
             val rn: ARMRegister,
             val rt: ARMRegister,
             val rm: ARMRegister,
             private val shiftT: SRType,
             private val shiftN: Int,
             size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rt, rm, size = size) {
    override val mnem = "LDRSB$mcnd"

    override fun execute() {
        val offset = Shift(rm.value(core), 32, shiftT, shiftN, core.cpu.flags.c.asInt)
        val offsetAddress = rn.value(core) + if (add) offset else -offset
        val address = if (index) offsetAddress else rn.value(core)
        rt.value(core, signext(core.inb(address like Datatype.DWORD), 8).asLong)
        if (wback) rn.value(core, offsetAddress)
    }
}