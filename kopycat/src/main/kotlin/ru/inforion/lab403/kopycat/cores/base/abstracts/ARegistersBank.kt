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
package ru.inforion.lab403.kopycat.cores.base.abstracts

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.proposal.toSerializable
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import java.io.Serializable
import java.util.*
import java.util.logging.Level
import kotlin.reflect.KProperty

/**
 * {RU}
 * Абстрактный класс банка (набора) регистров.
 *
 *
 * @param bits разрядность регистров
 * @property core ядро, в котором используются регистры
 * @property defs ?
 * @property total количество регистров
 * @property msb индекс наиболее значимого регистра
 * @property lsb индекс наименее значимого регистра
 * @property mask битовая маска, используемая при записи значения в регистр
 * @property data массив значений регистров
 * {RU}
 */
@Suppress("NOTHING_TO_INLINE")
abstract class ARegistersBank<T: AGenericCore, E: Enum<E>>(
        protected val core: T,
        private val defs: Array<E>,
        bits: Int
) : ICoreUnit, Iterable<Long> {
    companion object {
        @Transient val log = logger(Level.INFO)
    }

    @PublishedApi
    internal val regs = Array<ARegister<T>?>(defs.size) { null }

    @Deprecated("Will be removed 0.3.40")
    inline operator fun get(index: Int): ARegister<T> = regs[index]!!

    /**
     * {RU}
     * Значение регистра (прочитать/установить значение регистра)
     *
     * @property register регистр, с которым производится взаимодействие
     * {RU}
     */
    protected inner class valueOf<R: ARegister<T>>(val register: R): Serializable {
        operator fun getValue(thisRef: ARegistersBank<T, E>, property: KProperty<*>) = register.value(core)
        operator fun setValue(thisRef: ARegistersBank<T, E>, property: KProperty<*>, value: Long) = register.value(core, value)

        init {
            this@ARegistersBank.regs[register.reg] = register
        }
    }

    /**
     * {RU}
     * Отдельный бит регистра (прочитать/установить значение отдельного бита регистра)
     *
     * @property register регистр, с которым производится взаимодействие
     * @property bit индекс бита регистра
     * {RU}
     */
    protected inner class bitOf<R: ARegister<T>>(val register: R, val bit: Int): Serializable {
        operator fun getValue(thisRef: ARegistersBank<T, E>, property: KProperty<*>) = register.bit(core, bit) == 1
        operator fun setValue(thisRef: ARegistersBank<T, E>, property: KProperty<*>, value: Boolean) = register.bit(core, bit, value.asInt)
    }

    /**
     * {RU}
     * Последовательность бит регистра (прочитать/установить значение отдельного набора бит регистра)
     *
     * @property register регистр, с которым производится взаимодействие
     * @property rp нефиксированное число пар отображения бит регистра, с которым производится взаимодействие в значение последовательности бит поля
     *
     * @constructor создает последовательность бит регистра сложной конфигурации
     * {RU}
     */
    protected inner class fieldOf<R: ARegister<T>>(val register: R, vararg rp: Pair<IntRange, IntRange>): Serializable {
        /**
         * {RU}
         * @property register регистр, с которым производится взаимодействие
         * @property msb верхняя граница диапазона бит
         * @property lsb нижняя граница диапазона бит
         * {RU}
         */
        constructor(register: R, msb: Int, lsb: Int): this(register, msb..lsb to msb-lsb..0)
        val list = rp.map { (src, dst) -> src.toSerializable() to dst.toSerializable() }

        operator fun getValue(thisRef: ARegistersBank<T, E>, property: KProperty<*>): Long {
            var answer = 0L
            list.forEach { (src, dst) ->
                answer = answer.insert(register.bits(core, src.first..src.last), dst.first..dst.last)
            }
            return answer
        }
        operator fun setValue(thisRef: ARegistersBank<T, E>, property: KProperty<*>, value: Long) {
            list.forEach { (src, dst) -> register.bits(core, src.first..src.last, value[dst.first..dst.last]) }
        }
    }

    val total = defs.size
    val msb = bits - 1
    val lsb = 0
    private val mask = bitMask(msb..lsb)
    protected val data = LongArray(total)

    /**
     * {RU}
     * Метод читает данные регистра по индексу [index] в банке регистров без проверки различных
     * аппаратных условий.
     *
     * Внимание! используйте этот метод только в debugger
     * {RU}
     *
     * {EN}
     * Function reads the register data by it [index] in register bank without checking different
     * hardware conditions i.e. reading from random register or something else...
     *
     * WARNING: Should be used only (!) in debugger
     * {EN}
     */
    fun readIntern(index: Int): Long = data[index]

    /**
     * {RU}
     * Метод читает несколько бит [bits] по индексу [index] в банке регистров без проверки различных
     * аппаратных условий.
     *
     * Внимание! используйте этот метод только в debugger
     * {RU}
     *
     * {EN}
     * Function reads the register data range within [bits] by it [index] in register bank without checking different
     * hardware conditions i.e. reading from random register or something else...
     *
     * WARNING: Should be used only (!) in debugger
     * {EN}
     */
    fun readIntern(index: Int, bits: IntRange): Long = data[index][bits]

    /**
     * {RU}
     * Метод записывает данные в регистр по индексу [index] в банке регистров без проверки различных
     * аппаратных условий.
     *
     * Внимание! используйте этот метод только в debugger
     * {RU}
     *
     * {EN}
     * Function writes the register data by it [index] in register bank without checking different
     * hardware conditions i.e. writing to zero register or something else...
     *
     * WARNING: Should be used only (!) in debugger
     * {EN}
     */
    fun writeIntern(index: Int, value: Long) {
        data[index] = value and mask
    }

    /**
     * {RU}
     * Метод записывает несколько бит [bits] по индексу [index] в банке регистров без проверки различных
     * аппаратных условий.
     *
     * Внимание! используйте этот метод только в debugger
     * {RU}
     *
     * {EN}
     * Function writes the register data range within [bits] by it [index] in register bank without checking different
     * hardware conditions i.e. writing to zero register or something else...
     *
     * WARNING: Should be used only (!) in debugger
     * {EN}
     */
    fun writeIntern(index: Int, value: Long, bits: IntRange) {
        data[index] = data[index].insert(value, bits) and mask
    }

    /**
     * {RU}
     * Итератор по массиву регистров
     *
     * @return значение следующего регистра из массива регистров
     * {RU}
     */
    override operator fun iterator(): Iterator<Long> = object : Iterator<Long> {
        private var pos = 0

        override fun next(): Long {
            if (!hasNext()) {
                throw NoSuchElementException()
            }
            return data[pos++]
        }

        override fun hasNext(): Boolean {
            return pos < total
        }
    }

    /**
     * {RU}Сброс банка регистров{RU}
     */
    override fun reset() {
        super.reset()
        data.fill(0)
    }

    /**
     * {RU}
     * Строковое представление банка регистров
     *
     * @return строка-представление
     * {RU}
     */
    override fun stringify(): String {
        val cols = 3
        val chgrc = { indx: Int -> indx }

        val res = Array(total) { k ->
            "%s%4s[%2d] = %08X".format(if (k % cols == 0) "\n" else " ", defs[chgrc(k)].name, chgrc(k), data[chgrc(k)])
        }
        return res.joinToString(" ")
    }

    /**
     * {RU}
     * Сохранение состояния (сериализация)
     *
     * @param ctxt контекст объекта-сериализатора
     * @return отображение сохраняемых свойств объекта
     * {RU}
     */
    override fun serialize(ctxt: GenericSerializer) = defs.associate { it.name to data[it.ordinal].hex }

    /**
     * {RU}
     * Восстановление состояния (десериализация)
     *
     * @param ctxt контекст объекта-сериализатора
     * @param snapshot отображение восстанавливаемых свойств объекта
     * {RU}
     */
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        defs.forEach { def ->
            val value = snapshot[def.name]
            if (value != null) {
                log.finest { "Loading register ${def.name}[${def.ordinal}] value = $value" }
                data[def.ordinal] = (value as String).hexAsULong
            } else log.warning { "Register ${def.name}[${def.ordinal}] value not found! " +
                    "Possible your've made snapshot at earlie version -> results may be incorrect!" }
        }
    }
}