package ru.inforion.lab403.kopycat.device

import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts

class TestCore(parent: Module, name: String, frequency: Long = 77.MHz):
        ACore<TestCore, TestCPU, TestCOP>(parent, name, frequency, 1.0) {
    override fun abi(heap: LongRange, stack: LongRange): ABI<TestCore> =
            throw NotImplementedError("Operating system not supported")

    inner class Ports: ModulePorts(this) {
        val mem = Proxy("mem")
    }

    inner class Buses: ModuleBuses(this) {
        val mem = Bus("mem")
    }

    override val ports = Ports()
    override val buses = Buses()

    override val cpu = TestCPU(this, "cpu")
    override val cop = TestCOP(this, "cop")
    override val mmu = null
    override val fpu = null

    init {
        cpu.ports.mem.connect(buses.mem)
        ports.mem.connect(buses.mem)
    }
}