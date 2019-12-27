package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.modules.PIN
import java.util.logging.Level

/**
 * Created by r.valitov on 13.07.17.
 */
class FMI(parent: Module, name: String) : Module(parent, name) {
    companion object { private val log = logger(Level.ALL) }
    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem")
        val irq = Master("irq", PIN)
    }
    override val ports = Ports()

    private val FMI_1 = Register(ports.mem, 0x00, DWORD, "FMI_1", 0x0000_0000)
}