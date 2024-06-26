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
package ru.inforion.lab403.kopycat.cores.base.operands

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.QWORD
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.ANY
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Controls.VOID
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Type.VAR

open class Variable<T: AGenericCore>(default: ULong, dtyp: Datatype = DWORD) :
        AOperand<T>(VAR, ANY, VOID, WRONGI, dtyp) {

    private var value: ULong = default
    private var cf: Int = 0

    // Override and finalize value() method to forbid remove carry flag
    override fun value(core: T): ULong = value like dtyp
    // Maybe allow holding value unmasked?
    // value(core)-method mask it anyway, so we can get carry on demand from unmasked value
    // But we should check then other direct uses of `var value`
    override fun value(core: T, data: ULong) {
        value = data like dtyp
        cf = data[dtyp.bits].int
    }

    /**
     * {RU}Арифметические и логические операции с установкой флагов{RU}
     *
     * {EN}Arithmetic and logical operations with flags setup{EN}
     */
    // TODO: Add overflow flag support
    fun plus(core: T, op1: AOperand<T>, op2: AOperand<T>) = value(core, op1.value(core) + op2.value(core))
    fun plus(core: T, op1: AOperand<T>, op2: AOperand<T>, carry: Boolean) =
            value(core, op1.value(core) + op2.value(core) + carry.uint)

    fun minus(core: T, op1: AOperand<T>, op2: AOperand<T>) = value(core, op1.value(core) - op2.value(core))
    fun minus(core: T, op1: AOperand<T>, op2: AOperand<T>, carry: Boolean) =
            value(core, op1.value(core) - op2.value(core) - carry.uint)

    fun and(core: T, op1: AOperand<T>, op2: AOperand<T>) = value(core, op1.value(core) and op2.value(core))
    fun or(core: T, op1: AOperand<T>, op2: AOperand<T>) = value(core, op1.value(core) or op2.value(core))
    fun xor(core: T, op1: AOperand<T>, op2: AOperand<T>) = value(core, op1.value(core) xor op2.value(core))

    /**
     * {EN}Equals and hashCode{EN}
     */
    override fun equals(other: Any?): Boolean =
            other is Variable<*> &&
                    other.type == VAR &&
                    other.value == value &&
                    other.specflags == specflags

    override fun hashCode(): Int {
        var result = type.hashCode()
        result += 31 * result + value.hashCode()
        result += 31 * result + specflags.hashCode()
        return result
    }

    /**
     * {RU}
     * Возвращает бит переноса
     * @return бит переноса (carry)
     * {RU}
     *
     * {EN}
     * Returns carry bit (at index after the MSB)
     * NOTE: Be sure in value() implementation this bit isn't cut off
     * {EN}
     */
    @Suppress("UNUSED_PARAMETER")
    fun carry(core: T): Int = cf

    /**
     * {RU}Проверить установлен ли бит переноса{RU}
     *
     * {EN}
     * Check if carry bit is set
     * NOTE: See carry() function
     * {EN}
     */
    fun isCarry(core: T): Boolean {
        require(dtyp != QWORD) { "Use extended version of isCarry instead" }
        return carry(core) == 1
    }

    fun isCarry(core: T, op1: AOperand<T>, op2: AOperand<T>, isSubtract: Boolean) =
        if (dtyp == QWORD) isCarry64(core, op1, op2, isSubtract) else isCarry(core)


    private fun isCarry64(core: T, op1: AOperand<T>, op2: AOperand<T>, isSubtract: Boolean): Boolean {
        val a = op1.value(core)
        val b = op2.value(core)
        return if (isSubtract) a < b else (a >= -b) && b != 0uL
    }

    override fun toString(): String = if (value.long >= 0) "0x%X".format(value.long) else "-0x%X".format(value.long)

    /**
     * {EN}
     * Check if is overflow occurred in result variable (op2 - op1) or (op2 + op1)
     * core - core
     * op1 - operand1
     * op2 - operand2
     * sub - variable is result of subtract operation
     * {EN}
     */
    fun isIntegerOverflow(core: T, op1: AOperand<T>, op2: AOperand<T>, isSubtract: Boolean): Boolean =
            (op1.isNegative(core) && op2.isNegative(core) xor isSubtract && isNotNegative(core)) ||
                    (op1.isNotNegative(core) && op2.isNotNegative(core) xor isSubtract && isNegative(core))
}