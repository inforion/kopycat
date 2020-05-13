package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc

import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.interfaces.ITableEntry



class x86InstructionTable(
        private val rows: Int,
        private val cols: Int,
        private val rc: (x86OperandStream) -> Pair<Int, Int>,
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

    fun lookup(stream: x86OperandStream): ITableEntry? {
        val (row, col) = rc(stream)
        val entry = this[row, col]
        return if (entry is x86InstructionTable) entry.lookup(stream) else entry
    }
}