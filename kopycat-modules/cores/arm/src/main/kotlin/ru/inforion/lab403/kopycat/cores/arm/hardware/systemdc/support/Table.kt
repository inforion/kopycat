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
package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.support

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.base.exceptions.DecoderException
import ru.inforion.lab403.kopycat.interfaces.ITableEntry
import java.io.Serializable
import java.util.logging.Level

class Table(
        name: String,
        bits: Array<Any>,
        decoders: Array<Pair<String, ITableEntry?>>) : Stub(name) {

    companion object {
        @Transient private val log = logger(Level.INFO)
    }

    data class Entry(val ord: Int, val pattern: String, val mask: Mask, val table: ITableEntry?): Serializable

    constructor(name: String) : this(name, emptyArray(), emptyArray())

    private val entries: List<Entry> = decoders.mapIndexed { k, (pattern, table) ->
        val mask = Mask.fromPattern(pattern, bits)

        Entry(k, pattern, mask, table)
    }

    fun lookup(data: Long, where: Long): ADecoder<AARMInstruction> {
        var table = this
        var localEntries = entries
        do {
            val entry = localEntries.find { it.mask.suit(data) }
            log.fine { "decoding -> ${table.name} found $entry" }
            if (entry?.table == null) throw DecoderException(data, where, "last decoding table: $table")
            if (entry.table is ADecoder<*>)
                return entry.table
            else if (entry.table !is Table) throw DecoderException(data, where, "${entry.table}")
            table = entry.table
            localEntries = table.entries
        } while (true)
    }
}