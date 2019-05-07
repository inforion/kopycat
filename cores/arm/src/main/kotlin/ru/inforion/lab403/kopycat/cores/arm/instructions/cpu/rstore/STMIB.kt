package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rstore

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegisterList
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by the bat on 30.01.18
 */

class STMIB(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            val wback: Boolean,
            val rn: ARMRegister,
            val registers: ARMRegisterList,
            size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, registers, size = size) {
    override val mnem = "STMIB$mcnd"

    override fun execute() {
        var address = rn.value(core) + 4
        // There is difference from datasheet (all registers save in common loop) -> no PCStoreValue called
        registers.forEachIndexed { i, reg ->
            if (i == rn.reg && wback && i != registers.lowestSetBit) {
                throw ARMHardwareException.Unknown
            } else {
                core.outl(address, reg.value(core))
            }
            address += 4
        }
        if (wback) rn.value(core, rn.value(core) + 4 * registers.bitCount)
    }
}