/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.kopycat.cores.base.operands

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.READ
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Controls.VOID
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Type.IMM

/**
 * {RU}Константный операнд инструкции{RU}
 */
open class Immediate<in T: AGenericCore>(
    val value: ULong,
    val signed: Boolean = false,
    dtyp: Datatype = DWORD,
    num: Int = WRONGI
) : AOperand<T>(IMM, READ, VOID, num, dtyp) {

    /**
     * {RU}
     * Возвращает расширенное знаковое представление Immediate operand
     *
     * @see AOperand.ssext
     * @return расширенное знаковое представление Immediate operand
     * {RU}
     *
     * {EN}
     * Returns Java-SIGNED signed extension of Immediate operand
     * see AOperand method ssext(dev: T)
     * Note: this isn't mean sign comparison (use cast toInt)
     * {EN}
     */
    val ssext: Long get() = value.signextRenameMeAfter(dtyp.msb).long

    /**
     * {RU}Возвращает расширенное знаковое представление Immediate operand
     *
     * @see AOperand.usext
     * @return расширенное знаковое представление Immediate operand
     * {RU}
     *
     * {EN}
     * Returns Java-UNSIGNED signed extension of Immediate operand
     * see AOperand method usext(dev: T)
     * {EN}
     */
    val usext: ULong get() = value.signextRenameMeAfter(dtyp.msb)

    /**
     * {RU}Вернуть бит по его индексу{RU}
     */
    fun bit(index: Int) = value[index].int

    val msb get() = bit(dtyp.msb)
    val lsb get() = bit(dtyp.lsb)
    val isNegative get() = msb == 1
    val isNotNegative get() = !isNegative
    val isZero get() = value == 0uL
    val isNotZero get() = !isZero

    /**
     * {RU}Получить значение операнда{RU}
     */
    override fun value(core: T): ULong = value

    /**
     * {RU}Установить значение операнда{RU}
     */
    final override fun value(core: T, data: ULong): Unit = throw UnsupportedOperationException("Can't write to immediate value")

    override fun equals(other: Any?): Boolean =
            other is Immediate<*> &&
            other.type == IMM &&
            other.value == value &&
            other.signed == signed &&
            other.specflags == specflags

    override fun hashCode(): Int {
        var result = type.hashCode()
        result += 31 * result + value.hashCode()
        result += 31 * result + specflags.hashCode()
        return result
    }

    override fun toString() = when {
        signed -> when {
            isNegative -> {
                val value = -ssext like dtyp
                "-0x${value.hex}"
            }
            else -> "0x${ssext.hex}"
        }
        else -> "0x${value.hex}"
    }
}