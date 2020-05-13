package ru.inforion.lab403.kopycat.device

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.modules.memory.RAM

class TestDevice(parent: Module, name: String): Module(parent, name) {
    inner class Buses : ModuleBuses(this) { val mem = Bus("mem") }
    inner class Ports : ModulePorts(this) { val mem = Proxy("mem") }
    private val testCore = TestCore(this, "Test core")
    override val ports = Ports()
    override val buses = Buses()
    private val sram = RAM(this, "sram", 0x100)
    init {
        testCore.ports.mem.connect(buses.mem)
        ports.mem.connect(buses.mem)
        sram.ports.mem.connect(buses.mem, 0x0000_0000)
    }
}