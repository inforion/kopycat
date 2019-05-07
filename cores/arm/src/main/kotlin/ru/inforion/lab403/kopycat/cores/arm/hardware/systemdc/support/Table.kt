package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.support

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.base.exceptions.DecoderException
import ru.inforion.lab403.kopycat.interfaces.ITableEntry
import java.util.logging.Level

class Table(
        name: String,
        bits: Array<Any>,
        decoders: Array<Pair<String, ITableEntry?>>) : Stub(name) {

    companion object {
        private val log = logger(Level.INFO)
    }

    data class Entry(val ord: Int, val pattern: String, val mask: Mask, val table: ITableEntry?)

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