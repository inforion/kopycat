package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rstore

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegisterList
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



// PUSH (multiple registers), see A8.8.133
/** TODO: Merge with or replace [PUSH] */
class PUSHmr(cpu: AARMCore,
             opcode: Long,
             cond: Condition,
             val registers: ARMRegisterList,
             val unalignedAllowed: Boolean,
             size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, registers, size = size) {

    override val mnem = "PUSH$mcnd"

    override fun execute() {
        var address = core.cpu.regs.spMain.value - 4 * registers.bitCount
        // There is difference from datasheet (all registers save in common loop) -> no PCStoreValue called
        registers.forEachIndexed { _, reg ->
            // Skipping these lines
            // if i == 13 && i != LowestSetBit(registers) then // Only possible for encoding A1
            //      MemA[address,4] = bits(32) UNKNOWN;
            // Because of UNKNOWN
            core.outl(address like Datatype.DWORD, reg.value(core))
            address += 4
        }
        core.cpu.regs.spMain.value -= 4 * registers.bitCount
    }
}