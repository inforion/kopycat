package ru.inforion.lab403.kopycat.modules.armtest

import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.library.annotations.DontExportModule
import ru.inforion.lab403.kopycat.modules.BUS16
import ru.inforion.lab403.kopycat.modules.cores.ARMv7Core
import ru.inforion.lab403.kopycat.modules.debuggers.ARMDebugger
import ru.inforion.lab403.kopycat.modules.memory.RAM
import ru.inforion.lab403.kopycat.modules.memory.ROM
import java.io.File

@DontExportModule
class ARMTestUnit(
        parent: Module?,
        name: String,
        romPath: String): Module(parent, name) {

    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem", BUS16)
    }
    override val buses = Buses()

    val arm = ARMv7Core(this, "ARM", 100.MHz, 1.0)
    val sram = RAM(this, "sram", 0x0000_2000)
    val peripheral = RAM(this, "peripheral", 0x0002_5000)
    val rom = ROM(this, "rom", 0x8000, File(romPath))
    val dbg = ARMDebugger(this, "dbg")

    init {
        arm.ports.mem.connect(buses.mem)
        rom.ports.mem.connect(buses.mem, 0x0800_0000)
        sram.ports.mem.connect(buses.mem, 0x2000_0000)
        peripheral.ports.mem.connect(buses.mem, 0x4000_0000)
        dbg.ports.breakpoint.connect(buses.mem)
    }
}