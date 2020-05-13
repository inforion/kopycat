package ru.inforion.lab403.kopycat.modules.memory

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts

class VOID(parent: Module, name: String, val size: Int): Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", this@VOID.size)
    }

    override val ports = Ports()

    private val memory = Void(ports.mem, 0, size.asULong - 1, "VOID")
}