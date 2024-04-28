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
@file:Suppress("unused")

package ru.inforion.lab403.kopycat.cores.base.abstracts

import ru.inforion.lab403.common.extensions.unaryMinus
import ru.inforion.lab403.common.logging.FINER
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.interfaces.ITableEntry

/**
 * {RU}
 * Абстрактный класс инструкции процессора
 *
 * @param core ядро, в котором используется инструкция
 * @param type тип инструкции
 * @param operands массив операндов (произвольное количество)
 * {RU}
 */
@Suppress("INAPPLICABLE_JVM_NAME")
abstract class AInstruction<T : AGenericCore>(
    val core: T,
    val type: Type,
    @PublishedApi internal vararg val operands: AOperand<T>
) : Iterable<AOperand<T>>, ITableEntry {

    companion object {
        @Transient
        val log = logger(FINER)
    }

    /**
     * {EN}Possible operands type{EN}
     */
    enum class Type(val flags: Int) {
        /**
         * {EN}No specific use-case of instruction or none-instruction at all{EN}
         */
        VOID(0x0000),

        /**
         * {EN}Virtual instruction{EN}
         */
        VIRTUAL(0x0001),

        /**
         * {EN}Conditional jump instruction{EN}
         */
        COND(0x0010),

        /**
         * {EN}Indirect jump instruction{EN}
         */
        INDIRECT(0x0020),

        /**
         * {EN}Call instruction{EN}
         */
        CALL(0x0100),

        /**
         * {EN}Conditional call instruction{EN}
         */
        COND_CALL(0x0110),

        /**
         * {EN}Indirect call instruction{EN}
         */
        IND_CALL(0x0120),

        /**
         * {EN}Unconditional jump instruction{EN}
         */
        JUMP(0x1000),

        /**
         * {EN}Conditional jump instruction{EN}
         */
        COND_JUMP(0x1010),

        /**
         * {EN}Indirect jump instruction{EN}
         */
        IND_JUMP(0x1020),

        /**
         * {EN}Interrupt return instruction{EN}
         */
        IRET(0x10000),

        /**
         * {EN}Function return instruction{EN}
         */
        RET(0x20000);  // if applicable

        fun check(flags: Int) = (flags and this.flags) == this.flags
    }

    abstract val mnem: String

    /**
     * {RU}Выполнение инструкции{RU}
     */
    abstract fun execute()

    abstract val size: Int

    val opcount = operands.size

    @get:JvmName("getEa")
    var ea = -1uL

    inline val op1 get() = operands[0]
    inline val op2 get() = operands[1]
    inline val op3 get() = operands[2]
    inline val op4 get() = operands[3]
    inline val op5 get() = operands[4]
    inline val op6 get() = operands[5]

    init {
        operands.forEachIndexed { k, op -> op.num = k }
    }

    /**
     * {RU}
     * Проверка, является ли инструкция delay slot'ом
     *
     * @return true/false
     * {RU}
     */
    open fun isDelaySlot() = false

    /**
     * {RU}
     * Проверка наличия delay slot'а
     *
     * @return true/false
     * {RU}
     */
    open fun hasDelaySlot(type: Type, stop: Boolean) = false

    // Iterable interface implementation

    /**
     * {RU}
     * Получение операнда инструкции по индексу
     *
     * @param index индекс операнда
     * @return операнд
     * {RU}
     */
    operator fun get(index: Int): AOperand<T> {
        if (index >= opcount) throw IndexOutOfBoundsException()
        return operands[index]
    }

    /**
     * {RU}
     * Итератор по операндам инструкции
     *
     * @return следующий операнд из массива операндов
     * {RU}
     */
    final override operator fun iterator() = operands.iterator()

    /**
     * {RU}Строковое представление объекта{RU}
     */
    override fun toString(): String = "$mnem ${joinToString()}"
}