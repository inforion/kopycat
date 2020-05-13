package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.multiply

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.SInt
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class SMLAL(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            val flags: Boolean,
            val rdHi: ARMRegister,
            val rdLo: ARMRegister,
            val rm: ARMRegister,
            val rn: ARMRegister):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rdHi, rdLo, rm, rn) {
    override val mnem = "SMLAL$mcnd"

    val result = ARMVariable(Datatype.QWORD)
    override fun execute() {
        val acc = SInt(rdHi.value(core).shl(32) + rdLo.value(core), 64)
        result.value(core, SInt(rn.value(core), 32) * SInt(rm.value(core), 32) + acc)
        rdHi.value(core, result.value(core)[63..32])
        rdLo.value(core, result.value(core)[31..0])
        if (flags)
            FlagProcessor.processMulFlag(core, result)
    }
}