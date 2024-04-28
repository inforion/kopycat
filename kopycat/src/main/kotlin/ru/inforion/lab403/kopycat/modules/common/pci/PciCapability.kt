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
package ru.inforion.lab403.kopycat.modules.common.pci

import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.field

/**
 * Базовый класс PCI capability
 *
 * @param pci PCI устройство, к которому подключается capability
 * @param name название модуля
 * @param base адрес начала capability
 * @param next оффсет до следующей capability
 */
abstract class PciCapability(val pci: PciDevice, name: String, val base: ULong, val next: ULong) : Module(pci, name) {
    /** Размер структуры этой capability */
    abstract val size: ULong

    init {
        pci.capabilities.add(this)
    }

    /**
     * Начало структуры любой capability
     *
     * @param name название регистра
     * @param id id этой capability
     */
    inner class CapabilityIDClass(name: String, val id: Byte) : ByteAccessRegister(pci.ports.pci, base, Datatype.WORD, name) {
        /** Оффсет до следующей capability */
        private var NEXT by field(15..8)

        /** id этой capability */
        private var CID by field(7..0)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            NEXT = next
            CID = id.ulong_z
            return super.read(ea, ss, size)
        }
    }
}
