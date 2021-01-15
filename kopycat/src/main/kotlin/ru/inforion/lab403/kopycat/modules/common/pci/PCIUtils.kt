/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Non-free licenses may also be purchased from INFORION, LLC, 
 * for users who do not want their programs protected by the GPL. 
 * Contact us for details kopycat@inforion.ru
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package ru.inforion.lab403.kopycat.modules.common.pci

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.kopycat.cores.base.APort
import ru.inforion.lab403.kopycat.cores.base.Bus
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.modules.BUS44
import ru.inforion.lab403.kopycat.modules.BUS56
import ru.inforion.lab403.kopycat.modules.PCI_SPACES_COUNT

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