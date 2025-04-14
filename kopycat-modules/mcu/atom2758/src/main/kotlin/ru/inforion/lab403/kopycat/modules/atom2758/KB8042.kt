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

import ru.inforion.lab403.common.logging.CONFIG
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD

class KB8042(parent: Module, name: String) : Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val io = Port("io")
    }

    override val ports = Ports()

    private val KDATA = Register(ports.io, 0x60u, Datatype.BYTE, "KDATA", level = CONFIG)

    private val KDCTL = object : Register(ports.io, 0x64u, DWORD, "KDCTL", level = CONFIG) {
        var PERR by bit(7)
        var RxTO by bit(6)
        var TxTO by bit(5)
        var INH by bit(4)
        var A2 by bit(3)
        var SYS by bit(2)
        var IBF by bit(1)
        var OBF by bit(0)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            PERR = 0
            RxTO = 0
            TxTO = 0
            INH = 0
            SYS = 0
            IBF = 0
            OBF = 1
            return super.read(ea, ss, size)
        }
    }
}