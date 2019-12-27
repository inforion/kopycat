package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.Align
import ru.inforion.lab403.kopycat.cores.arm.ROR
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unknown
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister.GPR.PC
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.AARMCore.InstructionSet.ARM

/**
 * Created by a.gladkikh on 30.01.18
 */

class LDRL(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val add: Boolean,
           val rt: ARMRegister,
           val imm32: Immediate<AARMCore>,
           size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rt, imm32, size = size) {
    override val mnem = "LDR$mcnd"

    override fun execute() {
        val base = Align(PC.value(core), 4)
        val address = base + if (add) imm32.zext else -imm32.zext
        val data = core.inl(address)
        if (rt.reg == 15) { // PC
            if (address[1..0] == 0b00L) core.cpu.LoadWritePC(data)
            else throw Unpredictable
        } else if (core.cpu.UnalignedSupport() || address[1..0] == 0b00L) {
            rt.value(core, data)
        } else {
            if (core.cpu.CurrentInstrSet() == ARM) {
                rt.value(core, ROR(data, 32, 8 * address[1..0].asInt))
            } else {
                throw Unknown
            }
        }
    }
}