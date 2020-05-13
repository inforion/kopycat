package ru.inforion.lab403.kopycat.cores.base.operands

import ru.inforion.lab403.common.extensions.WRONGI
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Controls.VOID
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Type.DISPL

/**
 * {RU}
 * Операнд указатель на память.
 *
 * Адрес в памяти вычисляется путем сложения
 * значения регистра смещения и значения базового адреса.
 *
 * @param T шаблон типа AGenericCore
 * @property dtype тип данных
 * @property reg регистр, задающий смещение
 * @property off значение базового адреса
 * @property access параметр доступа
 * {RU}
 */
open class Displacement<in T: AGenericCore>(
        dtyp: Datatype,
        val reg: ARegister<T>,
        val off: Immediate<T>,
        access: Access,
        num: Int = WRONGI) :
        AOperand<T>(DISPL, access, VOID, num, dtyp) {

    // TODO: CHECK IT FOR X86 AND MIPS!!!!!!!!
    final override fun effectiveAddress(core: T): Long = (reg.value(core) + off.ssext(core)) like reg.dtyp

    /**
     * {RU}Получить значение операнда{RU}
     */
    override fun value(core: T): Long = core.read(dtyp, effectiveAddress(core))

    /**
     * {RU}Установить значение операнда{RU}
     */
    override fun value(core: T, data: Long): Unit = core.write(dtyp, effectiveAddress(core), data)

    override fun equals(other: Any?): Boolean =
            other is Displacement<*> &&
                    other.type == DISPL &&
                    other.dtyp == dtyp &&
                    other.reg == reg &&
                    other.off == off &&
                    other.specflags == specflags

    override fun hashCode(): Int {
        var result = type.hashCode()
        result += 31 * result + off.hashCode()
        result += 31 * result + dtyp.ordinal
        result += 31 * result + reg.hashCode()
        result += 31 * result + specflags.hashCode()
        return result
    }

    override fun toString(): String {
        val mspec = dtyp.name.toLowerCase()
        val sign = if (off.isNegative) "" else "+"
        return if (off.value != 0L) "$mspec [$reg$sign$off]" else "$mspec [$reg]"
    }
}