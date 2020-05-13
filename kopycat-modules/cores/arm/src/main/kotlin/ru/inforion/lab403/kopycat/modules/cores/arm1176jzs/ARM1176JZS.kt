package ru.inforion.lab403.kopycat.modules.cores.arm1176jzs

import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.ARMv6COP
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.ARMv6CPU
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.modules.BUS32
import ru.inforion.lab403.kopycat.modules.cores.AARMv6Core
import ru.inforion.lab403.kopycat.modules.memory.RAM

class ARM1176JZS constructor(parent: Module, name: String, frequency: Long, ipc: Double):
        AARMv6Core(parent, name, frequency, ipc) {


    inner class Buses: ModuleBuses(this) {
        val phys = Bus("phys")
        val virt = Bus("virt")
    }

    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem")
    }

    override val buses = Buses()
    override val ports = Ports()


//    inner class Buses : ModuleBuses(this) {
//        val mem = Bus("mem", BUS32)
//    }

//    private val sram = RAM(this, "SRAM", 0x0400_0000)
//
    init {
        cpu.ports.mem.connect(buses.virt)
        mmu.ports.inp.connect(buses.virt)

        mmu.ports.outp.connect(buses.phys)
        ports.mem.connect(buses.phys)

//        cpu.ports.mem.connect(buses.mem)
//        ports.mem.connect(buses.mem)
//        sram.ports.mem.connect(buses.mem, 0x10200000L)
    }
}