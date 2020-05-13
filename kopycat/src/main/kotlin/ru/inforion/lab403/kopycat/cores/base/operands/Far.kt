package ru.inforion.lab403.kopycat.cores.base.operands

import ru.inforion.lab403.common.extensions.WRONGI
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype

open class Far<in T: AGenericCore>(
        val address: Long,
        dtyp: Datatype,
        num: Int = WRONGI) :
        AOperand<T>(Type.FAR, Access.READ, Controls.VOID, num, dtyp) {

    override fun value(core: T): Long = address
    final override fun value(core: T, data: Long): Unit = throw UnsupportedOperationException("Can't write to far value")

    override fun equals(other: Any?): Boolean {
        if (other is Far<*>) {
            return (other.type == AOperand.Type.FAR &&
                    other.address == address &&
                    other.specflags == specflags)
        } else {
            return false
        }
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result += 31 * result + address.hashCode()
        result += 31 * result + specflags.hashCode()
        return result
    }

    override fun toString(): String = "%08X".format(address)
}