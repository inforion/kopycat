package ru.inforion.lab403.kopycat.modules.elanSC520

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.modules.BUS12
import java.util.logging.Level
import java.util.logging.Level.FINER


class Am5X86(parent: Module, name: String) : Module(parent, name) {
    companion object {
        val log = logger(FINER)
    }

    inner class Ports : ModulePorts(this) {
        val mmcr = Slave("mmcr", BUS12)
    }

    override val ports = Ports()

    val REVID = Register(ports.mmcr, 0, WORD, "REVID")
    val CPUCTL = Register(ports.mmcr, 2, WORD, "CPUCTL")
}