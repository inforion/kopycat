package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.ARMv6MCOP
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.ARMv6MCPU
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts



class ARMv6MCore constructor(parent: Module, name: String, frequency: Long, ipc: Double):
        AARMCore(parent, name, frequency, 6, ipc) {

    inner class Buses: ModuleBuses(this) {
        val mem = Bus("mem")
    }

    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem")
    }

    override val ports = Ports()
    override val buses = Buses()

    override val cpu = ARMv6MCPU(this, "cpu")
    override val cop = ARMv6MCOP(this, "cop")

    init {
        buses.connect(cpu.ports.mem, ports.mem)
    }
}