package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.ARMv6COP
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.ARMv6CPU
import ru.inforion.lab403.kopycat.cores.base.common.Module

/**
 * Created by the bat on 13.01.18.
 */

class ARMv6Core constructor(parent: Module, name: String, frequency: Long, ipc: Double):
        AARMCore(parent, name, frequency, 6, ipc) {

    override val cpu = ARMv6CPU(this, "cpu")
    override val cop = ARMv6COP(this, "cop")

    init {
        buses.connect(cpu.ports.mem, ports.mem)
    }
}