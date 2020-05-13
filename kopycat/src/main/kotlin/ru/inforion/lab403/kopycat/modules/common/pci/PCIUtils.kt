package ru.inforion.lab403.kopycat.modules.common.pci

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.kopycat.cores.base.APort
import ru.inforion.lab403.kopycat.cores.base.Bus
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.modules.*

fun pciBusDevicePrefix(bus: Int, device: Int) = PCIAddress(bus, device).prefix
fun pciFuncRegPrefix(func: Int, reg: Int) = PCIAddress(0, 0, func).prefix + reg

fun <T: APort>Array<T>.connect(spaces: Array<Bus>, bus: Int = 0, device: Int = 0) {
    val prefix = pciBusDevicePrefix(bus, device)
    // connect port for each space at bus/device offset
    zip(spaces).forEach { (port, bus) -> port.connect(bus, prefix) }
}

fun ModuleBuses.pci_bus(prefix: String) = buses(PCI_SPACES_COUNT, prefix, BUS56)

fun ModulePorts.pci_master(prefix: String) = masters(PCI_SPACES_COUNT, prefix, BUS56)
fun ModulePorts.pci_proxy(prefix: String) = proxies(PCI_SPACES_COUNT, prefix, BUS56)
fun ModulePorts.pci_slave(prefix: String) = slaves(PCI_SPACES_COUNT, prefix, BUS44)

fun ioSpaceBit(base: Long) = (base and 0x01).asInt

fun isIOSpace(base: Long) = ioSpaceBit(base) == 1