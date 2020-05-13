package ru.inforion.lab403.kopycat.modules.testbench

import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.modules.cores.ARMv6MCore
import ru.inforion.lab403.kopycat.modules.debuggers.ARMDebugger
import ru.inforion.lab403.kopycat.modules.memory.RAM

// Kopycat library manager looks for classes inherited from Module class
class Testbench(parent: Module?, name: String) : Module(parent, name) {

    // Add ARMv6 core into testbench device
    // First argument is parent module (where instantiated device fold)
    // Second argument is name (aka designator) can be any unique name
    val arm = ARMv6MCore(this, "arm", frequency = 10.MHz, ipc = 1.0)

    val dbg = ARMDebugger(this, "dbg")

    // Add modifiable memory region into testbench device (size = 1 MB)
    // Create internal buses description for testbench device
    // Buses are somelike wires and used to connect different parts of device
    val ram = RAM(this, "ram", size = 0x10_0000)

    // Assign new buses description to testbench device
    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem")
    }

    override val buses = Buses()

    // Make actual connection between CORE and RAM
    init {
        arm.ports.mem.connect(buses.mem)
        ram.ports.mem.connect(buses.mem, offset = 0x0000_0000)

        dbg.ports.breakpoint.connect(buses.mem)
        dbg.ports.reader.connect(buses.mem)
    }
}