package ru.inforion.lab403.kopycat.modules.common.pci

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.modules.*

data class PCIAddress(
        val bus: Int,
        val device: Int,
        val func: Int = 0,
        val reg: Int = 0,
        val enabled: Boolean = false
) {
    companion object {
        fun fromBDF(bdf: Int) = PCIAddress(
                bdf[PCI_BDF_BUS_RANGE],
                bdf[PCI_BDF_DEVICE_RANGE],
                bdf[PCI_BDF_FUNC_RANGE],
                bdf[PCI_BDF_REG_RANGE] and 0xFC,  // last two bits of reg should be 0 from I/O port
                bdf[PCI_BDF_ENA_BIT] == 1)

        fun fromPrefix(offset: Long) = fromBDF((offset ushr 32).asInt)
    }

    val bdf = insert(bus, PCI_BDF_BUS_RANGE)
            .insert(device, PCI_BDF_DEVICE_RANGE)
            .insert(func, PCI_BDF_FUNC_RANGE)
            .insert(reg, PCI_BDF_REG_RANGE)

    /**
     * Prefix to place bus
     */
    val prefix = bdf.asULong shl 32

    override fun toString() = "PCI[bus=$bus device=$device func=$func reg=$reg]"
}