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
package ru.inforion.lab403.kopycat.modules.atom2758

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import java.util.logging.Level

/**
 * Intel Atom Processor C2000 Product Family for Microserver
 * Table 30-2. I/O APIC Register Access and EOI Register, page 563
 */
class IOAPIC(parent: Module, name: String) : Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x1000)
    }

    override val ports = Ports()

    val IDX = Register(ports.mem, 0x0000u, Datatype.DWORD, "IOAPIC_IDX", level = Level.CONFIG)
    val WDW = Register(ports.mem, 0x0010u, Datatype.DWORD, "IOAPIC_WDW", level = Level.CONFIG)
    val EOI = Register(ports.mem, 0x0040u, Datatype.DWORD, "IOAPIC_EOI", level = Level.CONFIG)
}
