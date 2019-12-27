package ru.inforion.lab403.kopycat.cores.arm.operands

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.rotr32
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Type.CUSTOM
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.cores.arm.enums.GPR as eGPR

/**
 * Created by a.gladkikh on 13.01.18.
 * *** opcode-processing operands - Immediate ***
 */

class ARMImmediateCarry(cpu: AARMCore, opcode: Long) : AARMShift(cpu, opcode) {

    val imm8 = opcode[7..0]
    private val rimm = opcode[11..8].asInt
    private val shifterOperand = imm8 rotr32 (2 * rimm)

    override fun toString(): String = "#$shifterOperand"

    override fun equals(other: Any?): Boolean =
            other is ARMImmediateCarry &&
                    other.type == CUSTOM &&
                    other.dtyp == dtyp &&
                    other.opcode == opcode

    override fun hashCode(): Int {
        var result = type.hashCode()
        result += 31 * result + opcode.hashCode()
        result += 31 * result + dtyp.ordinal
        result += 31 * result + specflags.hashCode()
        return result
    }

    override fun value(core: AARMCore): Long = shifterOperand
    override fun carry(): Boolean = if (rimm == 0) cpu.cpu.flags.c else shifterOperand[31] == 1L
}