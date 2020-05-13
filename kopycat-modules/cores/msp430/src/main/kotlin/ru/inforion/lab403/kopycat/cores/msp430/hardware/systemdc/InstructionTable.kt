package ru.inforion.lab403.kopycat.cores.msp430.hardware.systemdc

import ru.inforion.lab403.kopycat.interfaces.ITableEntry



class InstructionTable(
        private val rows: Int,
        private val cols: Int,
        private val getRow: (Long) -> Long,
        private val getCol: (Long) -> Long,
        vararg entries: ITableEntry?) : ITableEntry {

    private val table = entries.copyOf()

    init {
        if (rows * cols != entries.size) {
            throw IllegalArgumentException("rows = %d * cols = %d not equal to %d".format(rows, cols, entries.size))
        }
    }

    private fun index(row: Int, col: Int): Int {
        return row * cols + col
    }

    operator fun get(row: Int, col: Int): ITableEntry? {
        return table[index(row, col)]
    }

    fun lookup(data: Long): ITableEntry? {
        val row = getRow(data).toInt()
        val col = getCol(data).toInt()
        val entry = this[row, col]
        return if (entry is InstructionTable) entry.lookup(data) else entry
    }
}