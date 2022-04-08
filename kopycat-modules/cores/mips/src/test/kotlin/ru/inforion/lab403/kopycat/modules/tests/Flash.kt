/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.kopycat.modules.tests

import ru.inforion.lab403.common.extensions.byte
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*

class Flash constructor(parent: Module, name: String): Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 16)
    }

    override val ports = Ports()

    private val flashSize = 0x00200000
    private val dataBuffer = ByteArray(flashSize) { (it and 0xFF).byte }

    private var ptr = -1

    val REG_SET_FLASH_PTR = object : Register(ports.mem, 0x00u, BYTE, "REG_SET_FLASH_PTR") {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) { ptr = value.int }
        override fun read(ea: ULong, ss: Int, size: Int) = ptr.ulong_z
    }

    val REG_READ_BYTE = object : Register(ports.mem, 0x04u, BYTE, "REG_READ_BYTE", writable = false) {
        override fun read(ea: ULong, ss: Int, size: Int) = dataBuffer[ptr++].ulong_z
    }

    val REG_WRITE_BYTE = object : Register(ports.mem, 0x08u, BYTE, "REG_WRITE_BYTE", readable = false) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            dataBuffer[ptr++] = (value and 0xFFu).byte
        }
    }
}