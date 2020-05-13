package ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.support

import ru.inforion.lab403.common.extensions.hex16
import ru.inforion.lab403.kopycat.cores.base.exceptions.DecoderException
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.ppc.hardware.systemdc.decoders.APPCDecoder
import ru.inforion.lab403.kopycat.interfaces.ITableEntry



class InstructionTable(
        private val rows: Int,
        private val cols: Int,
        private val getRow: (Long) -> Long,
        private val getCol: (Long) -> Long,
        vararg entries: ITableEntry?) : ATable() {

    private val table = entries.toMutableList()

    init {
        if (entries.isEmpty())
            table.addAll(MutableList(rows * cols) { null })
        else
            if (rows * cols != entries.size)
                throw IllegalArgumentException("rows = $rows * cols = $cols not equal to ${entries.size}")
    }

    operator fun plusAssign(other: InstructionTable) {
        if ((rows != other.rows) || (cols != other.cols))
            throw GeneralException("Instruction table size ($rows, $cols) not equal to other's (${other.rows}, ${other.cols})")

        for (i in 0 until other.table.size)
            if (other.table[i] != null) {
                if (table[i] != null)
                    throw GeneralException("Collision in table")
                table[i] = other.table[i]
            }
    }

    private fun index(row: Int, col: Int): Int {
        return row * cols + col
    }

    operator fun get(row: Int, col: Int): ITableEntry? {
        return table[index(row, col)]
    }

    override fun lookup(data: Long, where: Long): APPCDecoder {
        val row = getRow(data).toInt()
        val col = getCol(data).toInt()
        val entry = this[row, col]
        return when (entry) {
            is ATable -> entry.lookup(data, where)
            is APPCDecoder -> entry
            else -> throw DecoderException(data, where, "Decode failed")
        }
    }
}