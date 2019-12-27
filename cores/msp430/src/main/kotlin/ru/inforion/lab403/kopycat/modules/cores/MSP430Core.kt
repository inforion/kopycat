package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.msp430.hardware.processors.MSP430COP
import ru.inforion.lab403.kopycat.cores.msp430.hardware.processors.MSP430CPU
import ru.inforion.lab403.kopycat.modules.BUS16

/**
 * Created by a.kemurdzhian on 5/02/18.
 */

class MSP430Core constructor(parent: Module, name: String, frequency: Long):
        ACore<MSP430Core, MSP430CPU, MSP430COP>(parent, name, frequency, 1.0) {

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", BUS16)
    }

    inner class Buses: ModuleBuses(this) {
        val mem = Bus("mem", BUS16)
    }

    override val ports = Ports()
    override val buses = Buses()

    override val cpu = MSP430CPU(this, "cpu")
    override val cop = MSP430COP(this, "cop")
    override val mmu = null
    override val fpu = null

    override fun abi(heap: LongRange, stack: LongRange): ABI<MSP430Core> =
            throw NotImplementedError("Operating system not supported")

    init {
        cpu.ports.mem.connect(buses.mem)
        ports.mem.connect(buses.mem)
    }
}