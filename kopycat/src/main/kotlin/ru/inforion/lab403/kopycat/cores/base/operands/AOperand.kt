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
@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.cores.base.operands

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import java.io.Serializable
import java.math.BigInteger

@Suppress("INAPPLICABLE_JVM_NAME")
abstract class AOperand<in T: AGenericCore>(
    val type: Type,
    access: Access,
    controls: Controls,
    var num: Int,
    val dtyp: Datatype
): Serializable {
    companion object {
        @Transient val log = logger()
    }

    enum class Type(val id: Int) {
        VOID(0), REG(1), MEM(2), PHRASE(3),
        DISPL(4), IMM(5), FAR(6), NEAR(7),
        VAR(8), CUSTOM(9);
    }

    enum class Access(val flags: Int) {
        VOID(0x0000),
        READ(0x0001),
        WRITE(0x0002),
        ANY(READ.flags or WRITE.flags);
    }

    enum class Controls(val flags: Int) {
        VOID(0x0000),
        CALL(0x0010),
        JUMP(0x0020),
        STOP(0x0040);
    }

    open val hasEffectiveAddress = false

    val specflags = (access.flags or controls.flags)

    /**
     * {RU}Возвращает effective address для операндов в памяти например Memory, Displacement и Phrase{RU}
     *
     * {EN}Returns effective address of memory based operands e.g. Memory, Displacement and Phrase{EN}
     */
    @JvmName("effectiveAddress")
    open fun effectiveAddress(core: T): ULong = throw NotImplementedError("abstract operand has no effective address!")

    open val ssr get(): ARegisterNG<T> = throw NotImplementedError("abstract operand has no ssr!")

    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int
    abstract override fun toString(): String

    /**
     * {RU}Возвращает текущее значение операнда в соответствии с типом операнда{RU}
     *
     * {EN}Return current operand value according operand type{EN}
     */
    @JvmName("value")
    abstract fun value(core: T): ULong

    /**
     * {RU}Изменить текущее значение операнда в эмуляторе на указанное значение в соответствии с типом операнда{RU}
     *
     * {EN}Change current operand value in emulator to specified value according operand type{EN}
     */
    @JvmName("value")
    abstract fun value(core: T, data: ULong)

    @JvmName("extValue")
    open fun extValue(core: T): BigInteger = throw NotImplementedError("AOperand.extValue: BigInteger")

    @JvmName("extValue")
    open fun extValue(core: T, data: BigInteger): Unit = throw NotImplementedError("AOperand.extValue: Unit")

    @JvmName("bytes")
    open fun bytes(core: T, size: Int): ByteArray = throw NotImplementedError("AOperand.bytes: ByteArray")

    @JvmName("bytes")
    open fun bytes(core: T, data: ByteArray): Unit = throw NotImplementedError("AOperand.bytes: Unit")

    /**
     * {RU}Изменить текущее значение операнда в эмуляторе на значение другого указанного операнда в соответствии с типом операнда{RU}
     *
     * {EN}Change current operand value in emulator to specified other operand value according operand type{EN}
     */
    @Suppress("UNCHECKED_CAST")
    inline fun value(core: T, other: AOperand<*>): Unit = value(core, (other as AOperand<T>).value(core))

    /**
     * {RU}Декремент значения операнда{RU}
     */
    inline fun dec(core: T) = value(core, value(core) - 1u)

    /**
     * {RU}Инкремент значения операнда{RU}
     */
    inline fun inc(core: T) = value(core, value(core) + 1u)

    inline fun minus(core: T, data: ULong) = value(core, value(core) + data.cmpl2(dtyp.bits))
    inline fun plus(core: T, data: ULong) = value(core, value(core) + data)

    inline fun minus(core: T, data: UInt) = value(core, value(core) + data.cmpl2(dtyp.bits))
    inline fun plus(core: T, data: UInt) = value(core, value(core) + data)

    /**
     * {RU}Побитовая инверсия{RU}
     */
    inline fun inv(core: T) = value(core).inv() like dtyp

    /**
     * {RU}Извлечь один бит из значения операнда{RU}
     */
    inline fun bit(core: T, index: Int): Int = value(core)[index].int

    /**
     * {RU}Вставить один бит в значение операнда{RU}
     */
    inline fun bit(core: T, index: Int, value: Int) = value(core, value(core).insert(value.ulong_z, index))

    /**
     * {RU}Извлечь несколько бит из значения операнда{RU}
     */
    inline fun bits(core: T, range: IntRange): ULong = value(core)[range]

    /**
     * {RU}Вставить несколько бит в значение операнда{RU}
     */
    inline fun bits(core: T, range: IntRange, value: ULong) = value(core, value(core).insert(value, range))

    /**
     * {EN}
     * Returns "java UNSIGNED" signed-extension.
     * Use it only if you need a bitness extension (i.e. 32 -> 64, 16 -> 64).
     * i.e.
     *   Immediate(0xFFFF_FFE0).usext = 0xFFFF_FFFF_FFFF_FFE0uL
     *   Immediate(0x00E0).usext = 0xE0uL
     *
     *   // R0 = 0xDEAD_BEEF, R1 = 0x7654_3210
     *   Register(0).usext(core) = 0xFFFF_FFFF_DEAD_BEEFuL
     *   Register(1).usext(core) = 0x7654_3210uL
     * {EN}
     */
    inline fun usext(core: T): ULong = value(core) signext dtyp.msb

    /**
     * {EN}
     * Returns "java SIGNED" signed-extension. Use it only in case of signed comparisons.
     * i.e.
     *   Immediate(0xFFFF_FFE0).ssext = -0x20L
     *   Immediate(0x00E0).ssext = 0xE0L
     *
     *   // R0 = 0xDEAD_BEEF, R1 = 0x7654_3210
     *   Register(0).ssext(core) = -0x2152_4111L
     *   Register(1).ssext(core) = 0x7654_3210L
     * {EN}
     */
    inline fun ssext(core: T): Long = value(core).signext(dtyp.msb).long

    /**
     * {RU}@return дополненное нулями значение т.е. отсекает все биты старше MSB datatype бита{RU}
     *
     * {EN}@return zero-extended value i.e. cut off all bits above MSB datatype bit{EN}
     */
    inline fun zext(core: T): ULong = value(core) like dtyp

    /**
     * {RU}@return старший значащий бит операнда{RU}
     *
     * {EN}@return the most significant bit of operand value{EN}
     */
    inline fun msb(core: T): Int = bit(core, dtyp.msb)

    /**
     * {RU}@return младший значащий бит операнда{RU}
     *
     * {EN}@return the least significant bit of operand value{EN}
     */
    inline fun lsb(core: T): Int = bit(core, dtyp.lsb)

    /**
     * {RU}@return True если значение операнда отрицательное в соответствии с длинной datatype (не JAVA){RU}
     *
     * {EN}@return true if operand value is negative regarding to its datatype length (not JAVA){EN}
     */
    inline fun isNegative(core: T): Boolean = msb(core) == 1
    inline fun isNotNegative(core: T): Boolean = !isNegative(core)

    /**
     * {RU}@return True если значение операнда равно нулю в соответствии с длинной datatype (не полная длинна JAVA){RU}
     *
     * {EN}@return true if operand value is zero regarding to its datatype length (not full JAVA length){EN}
     */
    inline fun isZero(core: T): Boolean = zext(core) == 0uL
    inline fun isNotZero(core: T): Boolean = !isZero(core)

    /**
     * {RU}@return унарный минус{RU}
     *
     * {EN}@return unary minus{EN}
     */
    inline fun minus(core: T): ULong = value(core).cmpl2(dtyp.bits)

    /**
     * {RU}@return заданный byte (8-бит) из значения операнда{RU}
     *
     * {EN}@return specified byte (8-bit) from operand value{EN}
     */
    inline fun byte(core: T, index: Int): ULong {
        val hi = 8 * (index + 1) - 1
        val lo = 8 * index
        return bits(core, hi..lo)
    }

    /**
     * {RU}@return заданный word (16-бит) из значения операнда{RU}
     */
    inline fun word(core: T, index: Int): ULong {
        val hi = 16 * (index + 1) - 1
        val lo = 16 * index
        return bits(core, hi..lo)
    }

    inline fun ieee754(core: T): Double = value(core).ieee754()
    inline fun ieee754(core: T, data: Double) = value(core, data.ieee754AsUnsigned())

    inline val isWrite: Boolean get() = (specflags and Access.WRITE.flags) != 0
    inline val isRead: Boolean get() = (specflags and Access.WRITE.flags) != 0
    inline val isCall: Boolean get() = (specflags and Controls.CALL.flags) != 0
}