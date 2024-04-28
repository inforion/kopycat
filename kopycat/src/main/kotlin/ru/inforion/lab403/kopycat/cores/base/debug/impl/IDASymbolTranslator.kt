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
import ru.inforion.lab403.kopycat.library.types.Resource
import java.io.InputStream

class IDASymbolTranslator(stream: InputStream) : IDebugSymbolTranslator {
    companion object {
        fun InputStream.toIDASymbolTranslator() = IDASymbolTranslator(this)

        fun Resource.toIDASymbolTranslator() = openStream().toIDASymbolTranslator()
    }

    private enum class MapDataState { Begin, Segments, Symbols, End }

    private fun load(stream: InputStream): Collection<DebugSymbol> {
        val locRegex = Regex("loc_[0-9a-fA-F]{8}")

        val segments = mutableMapOf<ULong, ULong>()
        val mapping = mutableListOf<DebugSymbol>()

        var state = MapDataState.Begin

        stream.bufferedReader()
            .readLines()
            .filter { it.isNotEmpty() }
            .forEach { line ->
                when (state) {
                    MapDataState.Begin -> {
                        if (line.trim().startsWith("Start"))
                            state = MapDataState.Segments
                    }
                    MapDataState.Segments -> {
                        if (line.trim().startsWith("Address")) {
                            state = MapDataState.Symbols
                        } else {
                            val data = line.trim().split(' ', ':').filter { it.isNotEmpty() }
                            val secInd = data[0].ulongByHex
                            val secStart = data[1].ulongByHex
                            segments[secInd] = secStart
                        }
                    }
                    MapDataState.Symbols -> {
                        if (line.trim().startsWith("Program")) {
                            state = MapDataState.End
                        } else {
                            val data = line.trim().split(' ', ':').filter { it.isNotEmpty() }
                            val secInd = segments[data[0].ulongByHex]
                            if (secInd != null) {
                                val address = secInd + data[1].ulongByHex
                                val name = data.last()
                                if (!locRegex.matches(name))
                                    mapping.add(DebugSymbol(address, name))
                            }
                        }
                    }
                    MapDataState.End -> {

                    }
                }
            }

        return mapping
    }

    private val mapping = load(stream)

    override fun translate(address: ULong) = mapping.translate(address)
}