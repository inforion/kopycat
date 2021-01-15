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
package ru.inforion.lab403.kopycat.modules.common

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.proposal.toSerializable
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
        @Transient private val log = logger(Level.FINE)
    }
}