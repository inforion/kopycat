package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.support

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.exceptions.DecoderException
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders.APPCDecoder
import ru.inforion.lab403.kopycat.interfaces.ITableEntry
import java.util.logging.Level

class PatternTable(
        name: String,
        bits: Array<Any>,
        decoders: Array<Pair<String, ITableEntry?>>) : ATable() {

    companion object {
        private val log = logger(Level.INFO)
    }

    data class Entry(val ord: Int, val pattern: String, val mask: Mask, val table: ITableEntry?)

    constructor(name: String) : this(name, emptyArray(), emptyArray())

    private val entries: MutableList<Entry> = decoders.mapIndexed { k, (pattern, table) ->
        val mask = Mask.fromPattern(pattern, bits)

        Entry(k, pattern, mask, table)
    }.toMutableList()


    operator fun plusAssign(other: PatternTable) {
        for (e in other.entries) {
            val mInt = entries.find { it.mask.intersect(e.mask) }
            if (mInt != null)
                throw GeneralException("Entries have an intersection: ${mInt.mask} + ${e.mask} = ${mInt.mask + e.mask}")
        }
        entries.addAll(other.entries)
    }

    override fun lookup(data: Long, where: Long): APPCDecoder {
        //var table = this
        //var localEntries = entries
        //do {
        val entryTable = entries.find { it.mask.suit(data) }?.table ?: throw DecoderException(data, where, "last decoding table: $this")
        return when (entryTable) {
            is ATable -> entryTable.lookup(data, where)
            is APPCDecoder -> entryTable
            else -> throw DecoderException(data, where, "Decode failed")
        }
        //} while (true)
    }
}