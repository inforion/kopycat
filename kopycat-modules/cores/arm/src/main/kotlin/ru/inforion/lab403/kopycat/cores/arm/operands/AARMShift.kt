package ru.inforion.lab403.kopycat.cores.arm.operands

import ru.inforion.lab403.common.extensions.WRONGI
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

abstract class AARMShift(val cpu: AARMCore, val opcode: Long):
        AOperand<AARMCore>(Type.CUSTOM, Access.ANY, Controls.VOID, WRONGI, Datatype.DWORD) {

    override fun equals(other: Any?): Boolean =
            other is ARMImmediateCarry &&
                    other.type == Type.CUSTOM &&
                    other.dtyp == dtyp &&
                    other.opcode == opcode

    override fun hashCode(): Int {
        var result = type.hashCode()
        result += 31 * result + opcode.hashCode()
        result += 31 * result + dtyp.ordinal
        result += 31 * result + specflags.hashCode()
        return result
    }

    abstract fun carry(): Boolean

    final override fun value(core: AARMCore, data: Long): Unit =
            throw UnsupportedOperationException("Can't write to shift operand")
}