package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.ROR
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class LDRT(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val postindex: Boolean,
           val add: Boolean,
           val rn: ARMRegister,
           val rt: ARMRegister,
           val offset: AOperand<AARMCore>):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rt, offset) {
    override val mnem = "LDRT$mcnd"

    override fun execute() {
        if (core.cpu.CurrentModeIsHyp()) throw ARMHardwareException.Unpredictable
        val offsetAddress = rn.value(core) + if (add) offset.value(core) else -offset.value(core)
        val address = if (postindex) rn.value(core) else offsetAddress
        val data = core.inl(address like Datatype.DWORD)
        if (postindex) rn.value(core, offsetAddress)
        if (core.cpu.UnalignedSupport() || address[1..0] == 0b00L) {
            rt.value(core, data)
        } else {  // Can only apply before ARMv7
            if (core.cpu.CurrentInstrSet() == AARMCore.InstructionSet.ARM) {
                rt.value(core, ROR(data, 32, 8 * address[1..0].asInt))
            } else {
                throw ARMHardwareException.Unknown
            }
        }
    }
}