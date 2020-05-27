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