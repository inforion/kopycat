package ru.inforion.lab403.kopycat.modules.elanSC520

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.modules.BUS12
import ru.inforion.lab403.kopycat.modules.BUS16
import java.util.logging.Level

/**
 * System Arbiter Control
 */
class SAC(parent: Module, name: String) : Module(parent, name) {
    companion object {
        val log = logger(Level.FINER)
    }

    inner class Ports : ModulePorts(this) {
        val mmcr = Slave("mmcr", BUS12)
        val io = Slave("io", BUS16)
    }

    override val ports = Ports()

    private val SYSARBCTL = Register(ports.mmcr, 0x0070, BYTE, "SYSARBCTL")
    private val SYSARBMENB = Register(ports.mmcr, 0x0072, WORD, "SYSARBMENB")
    private val ARBPRICTL = Register(ports.mmcr, 0x0074, WORD, "ARBPRICTL")
}