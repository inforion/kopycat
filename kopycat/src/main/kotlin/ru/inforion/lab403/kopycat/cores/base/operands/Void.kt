package ru.inforion.lab403.kopycat.cores.base.operands

import ru.inforion.lab403.common.extensions.WRONGI
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.VOID


open class Void<T: AGenericCore>(num: Int = WRONGI) : AOperand<T>(Type.VOID, VOID, Controls.VOID, num, DWORD) {
    override fun value(core: T): Long = throw UnsupportedOperationException()
    override fun value(core: T, data: Long): Unit = throw UnsupportedOperationException()
    override fun equals(other: Any?): Boolean = other is Void<*>
    override fun hashCode(): Int = type.hashCode()
    override fun toString(): String = ""
}
