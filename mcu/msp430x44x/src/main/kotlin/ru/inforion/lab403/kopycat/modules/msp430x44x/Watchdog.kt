package ru.inforion.lab403.kopycat.modules.msp430x44x

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.modules.BUS16

/**
 * Created by a.kemurdzhian on 02/03/18.
 */

//Simple stub for watchdog
class Watchdog(parent: Module, name: String) : Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", BUS16)
    }

    override val ports = Ports()

    val WDG = Register(ports.mem, 0x120, WORD, "WDG")
}