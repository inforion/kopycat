package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.enums.GPR
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegisterList
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



// POP (ARM) (multiple registers), see A8.8.132
/** TODO: Merge with or replace [POP] */
class POPmr(cpu: AARMCore,
             opcode: Long,
             cond: Condition,
             val registers: ARMRegisterList,
             val unalignedAllowed: Boolean,
             size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, registers, size = size) {

    override val mnem = "POP$mcnd"

    override fun execute() {
        var address = core.cpu.regs.spMain.value
        // There is difference from datasheet (all registers load in common loop) -> no LoadWritePC called
        registers.forEachIndexed { _, reg ->
            if (reg.reg == GPR.PC.id)
                core.cpu.LoadWritePC(core.inl(address like Datatype.DWORD))
            else if (reg.reg == GPR.SPMain.id)
                core.cpu.regs.spMain.value = 0L // UNKNOWN
            else
                reg.value(core, core.inl(address like Datatype.DWORD))
            address += 4
        }
        core.cpu.regs.spMain.value += 4 * registers.bitCount
    }
}