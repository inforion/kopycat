package ru.inforion.lab403.kopycat.modules.cortexm0

import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.modules.cores.ARMv6Core
import ru.inforion.lab403.kopycat.modules.memory.RAM

class CORTEXM0(parent: Module, name: String) : Module(parent, name) {

    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem")
        val exc = Bus("exc")
    }

    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem")
        val irq = Proxy("irq", NVIC.INTERRUPT_COUNT)
    }

    override val ports = Ports()
    override val buses = Buses()

    private val arm  = ARMv6Core(this, "arm", 48.MHz, 0.9)
    private val sram = RAM(this, "sram", 0x8000)
    private val nvic = NVIC(this, "nvic")
    private val stk  = STK(this, "stk")
    private val scb  = SCB(this, "scb")

    init {
        ports.mem.connect(buses.mem)

        arm.ports.mem.connect(buses.mem)
        sram.ports.mem.connect(buses.mem, 0x0000_0000)
        nvic.ports.mem.connect(buses.mem, 0xE000_E100)
        stk.ports.mem.connect(buses.mem, 0xE000_E010)
        scb.ports.mem.connect(buses.mem, 0xE000_ED00)

        buses.connect(nvic.ports.irq, ports.irq)

        nvic.ports.exc.connect(buses.exc)
        scb.ports.irq.connect(buses.exc,  0)
        stk.ports.irq.connect(buses.exc, 15)
    }
}