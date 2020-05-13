package ru.inforion.lab403.kopycat.cores.arm.operands

import ru.inforion.lab403.common.extensions.WRONGI
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class ARMRegisterList(val cpu: AARMCore, val opcode: Long, val rbits: Long):
        AOperand<AARMCore>(Type.CUSTOM, Access.ANY, Controls.VOID, WRONGI, Datatype.DWORD),
        Iterable<ARMRegister> {

    private val regs = (0..15)
            .filter { rbits[it] == 1L }
            .map { GPRBank.Operand(it) }
            .toTypedArray()

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

    val bitCount: Int = regs.size
    val lowestSetBit: Int = regs.minBy { it.reg }!!.reg

    override operator fun iterator() = regs.iterator()

    override fun toString(): String = "{${regs.joinToString()}}"

    override fun value(core: AARMCore): Long =
            throw UnsupportedOperationException("Can't read value of registers list operand")

    override fun value(core: AARMCore, data: Long): Unit =
            throw UnsupportedOperationException("Can't write value to registers list operand")
}