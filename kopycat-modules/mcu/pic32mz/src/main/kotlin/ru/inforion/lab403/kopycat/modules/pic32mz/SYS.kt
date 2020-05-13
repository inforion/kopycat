package ru.inforion.lab403.kopycat.modules.pic32mz

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts


class SYS(parent: Module, name: String) : Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem")
    }

    override val ports = Ports()

    val CFGCON = ComplexRegister(ports.mem, 0x0000, "CFGCON")
    val DEVID = ComplexRegister(ports.mem, 0x0020, "DEVID")
    val SYSKEY = ComplexRegister(ports.mem, 0x0030, "SYSKEY")
    val CFGEBIA = ComplexRegister(ports.mem, 0x00C0, "CFGEBIA")
    val CFGEBIC = ComplexRegister(ports.mem, 0x00D0, "CFGEBIC")
    val CFGPG = ComplexRegister(ports.mem,0x00E0, "CFGPG")
}