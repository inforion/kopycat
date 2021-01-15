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

import ru.inforion.lab403.common.extensions.WRONGL
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.proposal.toSerializable
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInstruction.Type.*
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.interfaces.ITableEntry
import java.util.*
import java.util.logging.Level

/**
 * {RU}
 * Абстрактный класс инструкции процессора
 *
 *
 * @param T шаблон класса ядра
 * @property core ядро, в котором используется инструкция
 * @property type тип инструкции
 * @param operands массив операндов (произвольное количество)
 * @property mnem мнемоника инструкции
 * @property size размер инструкции в байтах
 * @property isJump флаг "инструкция перехода"
 * @property isBranch флаг "инструкция ветвления"
 * @property isIndirectJump флаг "инструкция косвенного перехода"
 * @property isCall флаг "инструкция вызова"
 * @property isCondCall флаг "инструкция условного вызова"
 * @property isIndirectCall флаг "инструкция косвенного вызова"
 * @property isCond флаг "условная инструкция"
 * @property isIndirect флаг "косвенная инструкция"
 * @property isIRet флаг "инструкция выхода из прерывания"
 * @property isRet флаг "инструкция возврата"
 * @property operands массив операндов
 * @property opcount количество операндов
 * @property ea адрес инструкции
 * @property op1 первый операнд инструкции
 * @property op2 второй операнд инструкции
 * @property op3 третий операнд инструкции
 * @property op4 четвертый инструкции
 * @property op5 пятый операнд инструкции
 * @property op6 шестой операнд инструкции
 * {RU}
 */
abstract class AInstruction<T: AGenericCore>(
        val core: T,
        val type: Type,
        vararg operands: AOperand<T>
) : Iterable<AOperand<T>>, ITableEntry {

    /**
     * {RU}
     * Класс-перечисление, описывающий *тип инструкции*
     *
     * @property flags флаги, характеризующие инструкцию
     * @property VOID void-инструкция
     * @property VIRTUAL виртуальная инструкция
     * @property COND условная инструкция
     * @property INDIRECT  ?
     * @property CALL инструкция вызова подпрограммы
     * @property COND_CALL условная инструкция вызова подпрограммы
     * @property IND_CALL ? инструкция вызова подпрограммы ?
     * @property JUMP инструкция безусловного перехода
     * @property COND_JUMP инструкция условного перехода
     * @property IND_JUMP инструкция перехода
     * @property IRET инструкция возврата из прерывания
     * @property RET инструкция возврата
     * {RU}
     */
    enum class Type(val flags: Int) {
        VOID(0x0000),
        VIRTUAL(0x0001),
        COND(0x0010),
        INDIRECT(0x0020),
        CALL(0x0100),
        COND_CALL(0x0110),
        IND_CALL(0x0120),
        JUMP(0x1000),
        COND_JUMP(0x1010),
        IND_JUMP(0x1020),
        IRET(0x10000),
        RET(0x20000);  // if applicable
    }

    /**
     * {RU}Объект-логгер{RU}
     */
    companion object {
        @Transient val log = logger(Level.FINER)
    }

    // Mandatory implementation part

    abstract val mnem: String

    /**
     * {RU}Выполнение инструкции{RU}
     */
    abstract fun execute()

    // Optional implementation part

    // open val size = 4
    abstract val size: Int

    val isJump = (type.flags and Type.JUMP.flags) != 0
    val isBranch = (type.flags and Type.COND_JUMP.flags) == Type.COND_JUMP.flags
    val isIndirectJump = (type.flags and Type.IND_JUMP.flags) == Type.IND_JUMP.flags
    val isCall = (type.flags and Type.CALL.flags) != 0
    val isCondCall = (type.flags and Type.COND_CALL.flags) == Type.COND_CALL.flags
    val isIndirectCall = (type.flags and Type.IND_CALL.flags) == Type.IND_CALL.flags
    val isCond = (type.flags and Type.COND.flags) != 0
    val isIndirect = (type.flags and Type.INDIRECT.flags) != 0
    val isIRet = (type.flags and Type.IRET.flags) != 0
    open val isRet = (type.flags and Type.RET.flags) != 0

    protected val operands = operands.copyOf()
    val opcount = operands.size

    var ea = WRONGL

    val op1: AOperand<T> get() = operands[0]
    val op2: AOperand<T> get() = operands[1]
    val op3: AOperand<T> get() = operands[2]
    val op4: AOperand<T> get() = operands[3]
    val op5: AOperand<T> get() = operands[4]
    val op6: AOperand<T> get() = operands[5]

    init {
        this.operands.forEachIndexed { k, op -> op.num = k }
    }

    /**
     * {RU}
     * Проверка, является ли инструкция delay slot'ом
     *
     * @return true/false
     * {RU}
     */
    open fun isDelaySlot(): Boolean = false

    /**
     * {RU}
     * Проверка наличия delay slot'а
     *
     * @return true/false
     * {RU}
     */
    open fun hasDelaySlot(type: Type, stop: Boolean): Boolean = false

    // Iterable interface implementation

    /**
     * {RU}
     * Получение операнда инструкции по индексу
     *
     * @param index индекс операнда
     * @return операнд
     * @throws IndexOutOfBoundsException
     * {RU}
     */
    operator fun get(index: Int): AOperand<T> {
        if (index >= opcount) {
            throw IndexOutOfBoundsException()
        }
        return operands[index]
    }

    /**
     * {RU}
     * Итератор по операндам инструкции
     *
     * @return следующий операнд из массива операндов
     * {RU}
     */
    override operator fun iterator(): Iterator<AOperand<T>> = object : Iterator<AOperand<T>> {
        private var pos = 0

        override fun next(): AOperand<T> {
            if (!hasNext()) {
                throw NoSuchElementException()
            }
            return operands[pos++]
        }

        override fun hasNext(): Boolean {
            return pos < opcount
        }
    }

    /**
     * {RU}Строковое представление объекта{RU}
     */
    override fun toString(): String = "$mnem ${joinToString()}"
}