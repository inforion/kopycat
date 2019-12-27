package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rstore

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister.GPR.SPMain
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegisterList
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by a.gladkikh on 30.01.18
 */

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
        val base = ARMRegister.gpr(core.cpu.StackPointerSelect().asInt).value(core)
        var address = base - 4 * registers.bitCount
        registers.forEachIndexed { i, reg ->
            if (reg.reg == 13 && i != registers.lowestSetBit) {  // SP
                throw ARMHardwareException.Unknown
            } else if (reg.reg == 15) {  // PC
                if (core.cpu.UnalignedSupport()) {
                    core.outl(address, core.cpu.PCStoreValue())
                } else {
                    TODO()
                    // MemA[address,4] = PCStoreValue();
                }
            } else {
                if (core.cpu.UnalignedSupport()) {
                    core.outl(address, reg.value(core))
                } else {
                    TODO()
                    // MemA[address,4] = R[i];
                }
            }
            address += 4
        }
        SPMain.value(core, base - 4 * registers.bitCount)
    }
}