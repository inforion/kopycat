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
package ru.inforion.lab403.kopycat.cores.base.debug.impl

import ru.inforion.lab403.common.extensions.ulongByHex
import ru.inforion.lab403.kopycat.cores.base.debug.DebugSymbol
import ru.inforion.lab403.kopycat.cores.base.debug.interfaces.IDebugSymbolTranslator
import ru.inforion.lab403.kopycat.cores.base.debug.translate
import java.io.InputStream

class GCCSymbolTranslator(stream: InputStream) : IDebugSymbolTranslator {
    companion object {
        fun InputStream.toGCCSymbolTranslator() = GCCSymbolTranslator(this)
    }

    private val mapping = stream.bufferedReader()
        .readLines()
        .filter { it.isNotEmpty() }
        .map { line ->
            val data = line.trim().split(' ')
            val address = data[0].ulongByHex
            val name = data.last()
            DebugSymbol(address, name)
        }

    override fun translate(address: ULong) = mapping.translate(address)
}