package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.system

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegisterList
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class LDM(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val wback: Boolean,
          val rn: ARMRegister,
          val registers: ARMRegisterList,
          size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, registers, size = size) {
    override val mnem = "LDM$mcnd"

    override fun execute() {
        var address = rn.value(core)
        // There is difference from datasheet (all registers load in common loop) -> no LoadWritePC called
        registers.forEachIndexed { _, reg ->
            if (reg.reg == core.cpu.regs.pc.reg) {
                core.cpu.LoadWritePC(core.inl(address like Datatype.DWORD))
            }
            else {
                reg.value(core, core.inl(address like Datatype.DWORD))
            }

            address += 4
        }
        if(wback && registers.rbits[rn.reg] == 0L) rn.value(core, rn.value(core) + 4 * registers.bitCount)
    }
}