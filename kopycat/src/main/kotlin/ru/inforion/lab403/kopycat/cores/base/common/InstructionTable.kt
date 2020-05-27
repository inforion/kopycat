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
package ru.inforion.lab403.kopycat.cores.base.common

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.interfaces.ITableEntry

/**
 * {RU}
 * Таблица инструкций.
 * Представляет собой контейнер (таблицу) для инструкций процессора.
 *
 *
 * @param entries массив объектов-инструкций (произвольное количество)
 *
 * @property rows количество строк таблицы
 * @property cols количество столбцов таблицы
 * @property getRow функция-обработчик для получения строки таблицы
 * @property getCol функция-обработчик для получения столбца таблицы
 *
 * @property table копия исходного массива инструкций
 * {RU}
 */
class InstructionTable(
        private val rows: Int,
        private val cols: Int,
        private val getRow: (Long) -> Long,
        private val getCol: (Long) -> Long,
        vararg entries: ITableEntry?) : ITableEntry {

    companion object {
        /**
         * {RU}
         * Подсчет количества комбинаций битов в указанном диапазоне
         *
         * @param range диапазон
         *
         * @return количество комбинаций
         * {RU}
         */
        private fun count(range: IntRange): Int = 1 shl (range.first - range.last + 1)
    }

    /**
     * {RU}
     * Конструктор таблицы из двух элементов
     *
     * @param bit номер элемента
     * @param entries vararg-массив инструкций
     * {RU}
     */
    constructor(bit: Int, vararg entries: ITableEntry?) :
            this (2, 1, { it[bit] }, { 0 }, *entries)

    /**
     * {RU}
     * Конструктор таблицы на основании диапазона [bits]
     *
     * @param bits диапазон для создания элементов
     * @param entries vararg-массив инструкций
     * {RU}
     */
    constructor(bits: IntRange, vararg entries: ITableEntry?) : this(
            count(bits), 1, { it[bits] }, { 0 }, *entries)

    /**
     * {RU}
     * Конструктор таблицы 2x2
     *
     * @param rbit номер бита в строке
     * @param cbit номер бита в столбце
     * @param entries vararg-массив инструкций
     * {RU}
     */
    constructor(rbit: Int, cbit: Int, vararg entries: ITableEntry?) :
            this(2, 2, { it[rbit] }, { it[cbit] }, *entries)

    /**
     * {RU}
     * Конструктор таблицы произвольного размера
     *
     * @param rbits диапазон битов по строке
     * @param cbits диапазон битов по столбцу
     * @param entries vararg-массив инструкций
     * {RU}
     */
    constructor(rbits: IntRange, cbits: IntRange, vararg entries: ITableEntry?) : this(
            count(rbits), count(cbits), { it[rbits] }, { it[cbits] }, *entries)

    private val table = entries.copyOf()

    init {
        if (rows * cols != entries.size)
            throw IllegalArgumentException("rows = %d * cols = %d not equal to %d".format(rows, cols, entries.size))
    }

    /**
     * {RU}
     * Сквозной индекс элемента по номеру строки и столбца
     *
     * @param row номер строки
     * @param col номер столбца
     *
     * @return сквозной номер элемента таблицы
     * {RU}
     */
    private fun index(row: Int, col: Int): Int = row * cols + col

    /**
     * {RU}
     * Элемент таблицы по номеру строки и столбца
     *
     * @param row номер строки
     * @param col номер столбца
     *
     * @return элемент таблицы или null
     * {RU}
     */
    operator fun get(row: Int, col: Int): ITableEntry? = table[index(row, col)]

    /**
     * {RU}
     * Поиск элемента таблицы по косвенным данным
     *
     * @param data данные для поиска в таблице
     *
     * @return элемент таблицы или null
     * {RU}
     */
    fun lookup(data: Long): ITableEntry? {
        val row = getRow(data).toInt()
        val col = getCol(data).toInt()
        val entry = this[row, col]
        return if (entry is InstructionTable) entry.lookup(data) else entry
    }
}