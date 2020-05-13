package ru.inforion.lab403.kopycat.modules.elanSC520

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.modules.common.pci.PCIHost
import java.util.logging.Level


class PCI(parent: Module, name: String): PCIHost(parent, name) {
    companion object {
        val log = logger(Level.FINER)
    }

    val HBCTL = Register(ports.mmcr, 0x60, WORD, "HBCTL")
    val HBTGTIRQCTL = Register(ports.mmcr, 0x62, WORD, "HBTGTIRQCTL")
    val HBTGTIRQSTA = Register(ports.mmcr, 0x64, WORD, "HBTGTIRQSTA")
    val HBMSTIRQCTL = Register(ports.mmcr, 0x66, WORD, "HBMSTIRQCTL")
    val HBMSTIRQSTA = Register(ports.mmcr, 0x68, WORD, "HBMSTIRQSTA")
    val MSTINTADD = Register(ports.mmcr, 0x6C, WORD, "MSTINTADD")
}