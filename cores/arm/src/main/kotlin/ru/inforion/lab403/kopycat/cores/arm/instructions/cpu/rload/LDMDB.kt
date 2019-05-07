package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unknown
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegisterList
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by the bat on 30.01.18
 */

class LDMDB(core: AARMCore,
            opcode: Long,
            cond: Condition,
            val wback: Boolean,
            val rn: ARMRegister,
            val registers: ARMRegisterList,
            size: Int):
        AARMInstruction(core, Type.VOID, cond, opcode, rn, registers, size = size) {
    override val mnem = "LDMDB$mcnd"

    override fun execute() {
        var address = rn.value(core) - 4 * registers.bitCount
        // There is difference from datasheet (all registers save in common loop) -> no LoadWritePC called
        registers.forEachIndexed { i, reg ->
            if (i == rn.reg && wback && i != registers.lowestSetBit) {
                throw Unknown
            } else {
                reg.value(core, core.inl(address))
            }
            address += 4
        }
        if(wback) {
            if (registers.rbits[rn.reg] == 0L) rn.value(core, rn.value(core) - 4 * registers.bitCount)
            else throw Unknown
        }
    }
}