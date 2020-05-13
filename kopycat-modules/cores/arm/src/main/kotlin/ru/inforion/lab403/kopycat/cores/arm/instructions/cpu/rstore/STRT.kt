package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rstore

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.AARMCore.InstructionSet.ARM



class STRT(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val postindex: Boolean,
           val add: Boolean,
           val rn: ARMRegister,
           val rt: ARMRegister,
           val offset: AOperand<AARMCore>):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rt, offset) {
    override val mnem = "STRT$mcnd"

    override fun execute() {
        if (core.cpu.CurrentModeIsHyp()) throw ARMHardwareException.Unpredictable

        val offsetAddress = rn.value(core) + if (add) offset.value(core) else -offset.value(core)
        val address = if (postindex) rn.value(core) else offsetAddress
        val data = if(rt.reg == core.cpu.regs.pc.reg) core.cpu.PCStoreValue() else rt.value(core)

        if(core.cpu.UnalignedSupport() || address[1..0] == 0b00L || core.cpu.CurrentInstrSet() == ARM)
            core.outl(address like Datatype.DWORD, data)
        else throw ARMHardwareException.Unknown
        if (postindex) rn.value(core, offsetAddress)
    }
}