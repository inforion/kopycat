package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.unconditional

import ru.inforion.lab403.common.extensions.clr
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.extensions.set
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.AARMv6Core



// See A8.8.126
class PLD(val cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val rn: ARMRegister,
          val imm32: Long,
          val add: Boolean,
          val is_pldw: Boolean):
        AARMInstruction(cpu, Type.VOID, cond, opcode) {
    override val mnem = "PLD"

    override fun execute() {
        // TODO: data preload isn't implemented
//        val address = if (add) (rn.value(core) + imm32) else (rn.value(core) - imm32)
//        if (is_pldw)
//            Hint_PreloadDataForWrite(address)
//        else
//            Hint_PreloadData(address)
    }
}