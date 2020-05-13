package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rstore

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegisterList
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class PUSH(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val rn: ARMRegister,
           private val unalignedAllowed: Boolean,
           val registers: ARMRegisterList,
           size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, registers, size = size) {
    override val mnem = "PUSH$mcnd"

    override fun execute() {
        val sp = core.cpu.StackPointerSelect()
        val newBaseValue = rn.value(core) - 4 * registers.bitCount
        var address = newBaseValue
        registers.forEachIndexed { i, reg ->
            if (reg.reg == sp && i != registers.lowestSetBit) {  // SP
                throw ARMHardwareException.Unknown
            } else if (reg.reg == core.cpu.regs.pc.reg) {  // PC
                if (core.cpu.UnalignedSupport()) {
                    core.outl(address like Datatype.DWORD, core.cpu.PCStoreValue())
                } else {
                    TODO()
                    // MemA[address,4] = PCStoreValue();
                }
            } else {
                if (core.cpu.UnalignedSupport()) {
                    core.outl(address like Datatype.DWORD, reg.value(core))
                } else {
                    TODO()
                    // MemA[address,4] = R[i];
                }
            }
            address += 4
        }
        rn.value(core, newBaseValue)
    }
}