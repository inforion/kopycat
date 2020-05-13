package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.ARMv7COP
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.ARMv7CPU
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts



class ARMv7Core constructor(parent: Module, name: String, frequency: Long, ipc: Double):
        AARMCore(parent, name, frequency, 7, ipc) {

    inner class Buses: ModuleBuses(this) {
        val mem = Bus("mem")
    }

    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem")
    }

    override val ports = Ports()
    override val buses = Buses()

    enum class Endianess(val code: Int) {
        BIG_ENDIAN(1),
        LITTLE_ENDIAN(0);
        companion object {
            fun from(code: Int): Endianess = values().first { it.code == code }
        }
    }

    override val cpu = ARMv7CPU(this, "cpu")
    override val cop = ARMv7COP(this, "cop")

    init {
        buses.connect(cpu.ports.mem, ports.mem)
    }
}