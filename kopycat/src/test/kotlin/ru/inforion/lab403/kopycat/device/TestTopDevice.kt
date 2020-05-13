package ru.inforion.lab403.kopycat.device

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.modules.memory.RAM

class TestTopDevice(parent: Module, name: String): Module(parent, name) {
    inner class Ports : ModulePorts(this)
    inner class Buses : ModuleBuses(this) { val mem = Bus("mem") }
    override val ports = Ports()
    override val buses = Buses()
    private val device = TestDevice(this, "test device")
    private val rom = RAM(this, "rom", 0x100)
    private val sram = RAM(this, "sram", 0x100)
    init {
        device.ports.mem.connect(buses.mem)
        rom.ports.mem.connect(buses.mem, 0x0800_0000)
        sram.ports.mem.connect(buses.mem, 0x2000_0000)
    }
}