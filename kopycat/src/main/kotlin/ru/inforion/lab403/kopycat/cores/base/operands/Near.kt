package ru.inforion.lab403.kopycat.cores.base.operands

import ru.inforion.lab403.common.extensions.WRONGI
import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.READ
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Controls.VOID
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Type.NEAR

open class Near<in T: AGenericCore>(val offset: Int, dtyp: Datatype, num: Int = WRONGI) :
        AOperand<T>(NEAR, READ, VOID, num, dtyp) {

    override fun value(core: T): Long = offset.toULong()
    override fun toString(): String = "%08X".format(offset)

    final override fun value(core: T, data: Long): Unit = throw UnsupportedOperationException("Can't write to near value")

    override fun equals(other: Any?): Boolean =
            other is Near<*> &&
            other.type == NEAR &&
            other.offset == offset &&
            other.specflags == specflags

    override fun hashCode(): Int {
        var result = type.hashCode()
        result += 31 * result + offset.hashCode()
        result += 31 * result + specflags.hashCode()
        return result
    }
}