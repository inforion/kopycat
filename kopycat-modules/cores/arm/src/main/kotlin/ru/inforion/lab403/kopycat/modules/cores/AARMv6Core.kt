package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.ARMv6COP
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.ARMv6CPU
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.ARMv6MMU
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.modules.BUS32

abstract class AARMv6Core(parent: Module, name: String, frequency: Long, ipc: Double) :
        AARMCore(parent, name, frequency, 6, ipc) {

    override val cpu = ARMv6CPU(this, "cpu", haveVirtExt = true)
    override val cop = ARMv6COP(this, "cop")

    override val mmu = ARMv6MMU(this, "mmu")
}