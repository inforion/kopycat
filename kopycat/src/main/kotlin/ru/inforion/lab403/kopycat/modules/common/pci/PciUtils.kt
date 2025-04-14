/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
@file:Suppress("FunctionName", "NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.modules.common.pci

import ru.inforion.lab403.kopycat.cores.base.APort
import ru.inforion.lab403.kopycat.cores.base.Bus
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.modules.BUS16
import ru.inforion.lab403.kopycat.modules.BUS32

fun <T: APort> T.pci_connect(pci: Bus, bus: Int, device: Int, func: Int) =
    connect(pci, PciConfigAddress(bus, device, func).offset)

fun <T : APort> T.pci_disconnect(pci: Bus) = disconnect(pci)

fun ModuleBuses.pci_bus(prefix: String) = Bus(prefix)

fun ModulePorts.pci_master(prefix: String) = Port(prefix)
fun ModulePorts.pci_proxy(prefix: String) = Proxy(prefix)
fun ModulePorts.pci_slave(prefix: String) = Port(prefix)