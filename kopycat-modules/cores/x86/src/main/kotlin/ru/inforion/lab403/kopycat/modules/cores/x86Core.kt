package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86COP
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86FPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86MMU
import ru.inforion.lab403.kopycat.cores.x86.x86ABI
import ru.inforion.lab403.kopycat.modules.BUS16
import ru.inforion.lab403.kopycat.modules.BUS32


class x86Core constructor(parent: Module, name: String, frequency: Long, val generation: Generation, ipc: Double):
        ACore<x86Core, x86CPU, x86COP>(parent, name, frequency, ipc) {
    enum class Generation { i8086, i186, i286, i386, i486, Am5x86, Pentium }

    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem")
        val io = Master("io", BUS16)
    }

    inner class Buses: ModuleBuses(this) {
        val physical = Bus("physical")
        val virtual = Bus("virtual")
    }

    override val ports = Ports()
    override val buses = Buses()

    override val cpu = x86CPU(this, "cpu")
    override val cop = x86COP(this, "cop")
    override val mmu = x86MMU(this, "mmu")
    override val fpu = x86FPU(this, "fpu")

    override fun abi(heap: LongRange, stack: LongRange): ABI<x86Core> = x86ABI(this, heap, stack, false)

    init {
        cpu.ports.mem.connect(buses.virtual)
        mmu.ports.inp.connect(buses.virtual)

        mmu.ports.outp.connect(buses.physical)
        ports.mem.connect(buses.physical)
    }
}