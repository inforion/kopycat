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
package ru.inforion.lab403.kopycat.modules.terminals

import ru.inforion.lab403.common.extensions.asUInt
import ru.inforion.lab403.kopycat.cores.base.common.Module
import java.io.File
import java.io.OutputStream

class UartStreamTerminal(parent: Module, name: String, val stream: OutputStream) : UartTerminal(parent, name) {
    constructor(parent: Module, name: String, file: File) : this(parent, name, file.outputStream())
    constructor(parent: Module, name: String, path: String) : this(parent, name, File(path))

    private val writer = stream.writer()

    override fun onByteTransmitReady(byte: Byte) {
        writer.write(byte.asUInt)
        writer.flush()
    }

//    init {
//        terminalReceiveEnabled = false
//    }
}