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
        val value: Long,
        val signed: Boolean = false,
        dtyp: Datatype = DWORD,
        num: Int = WRONGI) :
        AOperand<T>(IMM, READ, VOID, num, dtyp) {

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
    val ssext: Long get() = signext(value, dtyp.bits).asLong

    /**
     * {RU}Возвращает расширенное знаковое представление Immediate operand
     *
     * @see AOperand.sext
     * @return расширенное знаковое представление Immediate operand
     * {RU}
     *
     * {EN}
     * Returns Java-UNSIGNED signed extension of Immediate operand
     * see AOperand method sext(dev: T)
     * {EN}
     */
    val usext: Long get() = signext(value, dtyp.bits).asULong

    /**
     * {RU}
     * Возвращает дополненное нулями значение
     *
     * @see AOperand.zext
     * @return дополненное нулями значение
     * {RU}
     *
     * {EN}
     * see AOperand method zext(dev: T)
     * Get zero-extended long
     * {EN}
     */
    val zext: Long get() = value like dtyp

    /**
     * {RU}Вернуть бит по его индексу{RU}
     */
    fun bit(index: Int): Int = value[index].asInt
    val msb: Int get() = bit(dtyp.msb)
    val lsb: Int get() = bit(dtyp.lsb)
    val isNegative: Boolean get() = msb == 1
    val isNotNegative: Boolean get() = !isNegative
    val isZero: Boolean get() = zext == 0L
    val isNotZero: Boolean get() = !isZero

    /**
     * {RU}Получить значение операнда{RU}
     */
    override fun value(core: T): Long = value

    /**
     * {RU}Установить значение операнда{RU}
     */
    final override fun value(core: T, data: Long): Unit = throw UnsupportedOperationException("Can't write to immediate value")

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

    override fun toString(): String = if (signed) {
        if (isNotNegative) {
            "0x%X".format(ssext)
        } else {
            // because java always format as unsigned
            "-0x%X".format(-ssext)
        }
    } else "0x%X".format(zext)
}