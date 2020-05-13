package ru.inforion.lab403.kopycat.modules.cortexm0

import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.modules.cores.ARMv6MCore
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

    private val arm  = ARMv6MCore(this, "arm", 48.MHz, 0.9)
    private val nvic = NVIC(this, "nvic")
    private val stk  = STK(this, "stk")
    private val scb  = SCB(this, "scb")

    override fun reset() {
        super.reset()
        val sp = core.inl(0x0000_0000)
        val pc = core.inl(0x0000_0004)
        log.info { "Setup CORTEX-M0 core PC=0x${pc.hex8} MSP=0x${sp.hex8}" }
        arm.cpu.BXWritePC(pc)
        arm.cpu.regs.spMain.value = sp
    }

    init {
        ports.mem.connect(buses.mem)

        arm.ports.mem.connect(buses.mem)
        nvic.ports.mem.connect(buses.mem, 0xE000_E100)
        stk.ports.mem.connect(buses.mem, 0xE000_E010)
        scb.ports.mem.connect(buses.mem, 0xE000_ED00)

        buses.connect(nvic.ports.irq, ports.irq)

        nvic.ports.exc.connect(buses.exc)
        scb.ports.irq.connect(buses.exc,  0)
        stk.ports.irq.connect(buses.exc, 15)
    }
}