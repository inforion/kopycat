package ru.inforion.lab403.kopycat.modules.common

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.modules.common.pci.PCITarget
import java.util.logging.Level

@Suppress("unused", "PropertyName", "ClassName")

class Am79C972(parent: Module, name: String) : PCITarget(
        parent,
        name,
        0x1022,
        0x2000,
        0x0000,
        0x0290,
        0x30,
        0x200000,  // ethernet controller
        0,
        0,  // disabled
        0x0000,
        0x0000,
        0,
        0x40,
        0,
        1,
        0x06,
        0xFF,
        0x00000001L to 0x20,  // CSRMap (4 Kb)
        0x00000000L to 0x1000  // CSRIO (32 bytes)
) {
    companion object {
        private val log = logger(Level.FINE)
    }
}