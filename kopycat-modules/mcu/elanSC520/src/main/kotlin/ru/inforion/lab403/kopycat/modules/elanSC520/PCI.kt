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
package ru.inforion.lab403.kopycat.modules.elanSC520

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.modules.common.pci.PCIHost
import java.util.logging.Level


class PCI(parent: Module, name: String): PCIHost(parent, name) {
    companion object {
        @Transient val log = logger(Level.FINER)
    }

    val HBCTL = Register(ports.mmcr, 0x60, WORD, "HBCTL")
    val HBTGTIRQCTL = Register(ports.mmcr, 0x62, WORD, "HBTGTIRQCTL")
    val HBTGTIRQSTA = Register(ports.mmcr, 0x64, WORD, "HBTGTIRQSTA")
    val HBMSTIRQCTL = Register(ports.mmcr, 0x66, WORD, "HBMSTIRQCTL")
    val HBMSTIRQSTA = Register(ports.mmcr, 0x68, WORD, "HBMSTIRQSTA")
    val MSTINTADD = Register(ports.mmcr, 0x6C, WORD, "MSTINTADD")
}