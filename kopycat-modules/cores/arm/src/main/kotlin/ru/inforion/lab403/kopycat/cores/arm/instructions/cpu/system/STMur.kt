package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.system

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.enums.GPR
import ru.inforion.lab403.kopycat.cores.arm.enums.ProcessorMode
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.RegisterBanking
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegisterList
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



//STM (User registers), see B9.3.17
class STMur(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            val increment: Boolean,
            val wordhigher: Boolean,
            val rn: ARMRegister,
            val registers: ARMRegisterList,
            size: Int): AARMInstruction(cpu, Type.VOID, cond, opcode, rn, registers, size = size) {
    override val mnem = "STM$mcnd"

    override fun execute() {
        if (core.cpu.CurrentModeIsHyp())
            throw ARMHardwareException.Undefined
        else if (core.cpu.CurrentModeIsUserOrSystem())
            throw ARMHardwareException.Unpredictable
        else {
            val length = 4 * registers.bitCount
            var address = if (increment) rn.value(core) else rn.value(core) - length
            if (wordhigher) address += 4
            if (core.cpu.sregs.cpsr.m == ProcessorMode.fiq.id.toLong())
                TODO("Write user regs from r8 to r12")
            registers.forEachIndexed { _, reg ->
                if (reg.reg < GPR.SPMain.id)
                    core.outl(address like Datatype.DWORD, reg.value(core))
                else
                /** Class [RegisterBanking] contains registers from r8 to lr */
                    core.outl(address like Datatype.DWORD, core.cpu.banking[ProcessorMode.usr.ordinal].read(reg.reg - 8))
                address += 4
            }
        }
    }
}