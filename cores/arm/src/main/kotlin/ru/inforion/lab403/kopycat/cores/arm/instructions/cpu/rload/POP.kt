package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unknown
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegisterList
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

/**
 * Created by the bat on 30.01.18
 */

class POP(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val rn: ARMRegister,
          private val unalignedAllowed: Boolean,
          val registers: ARMRegisterList,
          size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, registers, size = size) {
    override val mnem = "POP$mcnd"

    override fun execute() {
        var address = rn.value(core)

        // Выравниваем стэк до вызова LoadWritePC(), иначе не заработают прерывания
        if(registers.contains(rn)) throw Unknown
        else rn.value(core, rn.value(core) + 4 * registers.bitCount)

        // There is difference from datasheet (all registers save in common loop) -> no LoadWritePC called
        registers.forEachIndexed { _, reg ->
            if(reg.reg != 15){
                reg.value(core, core.inl(address))
                address += 4
            } else {
                if(unalignedAllowed)
                    if (address[1..0] == 0L) core.cpu.LoadWritePC(core.inl(address))
                    else throw Unpredictable
                else
                    core.cpu.LoadWritePC(core.inl(address))
            }
        }
    }
}