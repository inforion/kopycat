package ru.inforion.lab403.kopycat.cores.arm.operands

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.arm.RRX
import ru.inforion.lab403.kopycat.cores.arm.enums.ShiftType
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Type.CUSTOM
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.cores.arm.enums.GPR as eGPR

/**
 * Created by a.gladkikh on 13.01.18.
 */

class ARMImmediateShift(cpu: AARMCore, opcode: Long) : AARMShift(cpu, opcode) {

    val rm = ARMRegister.gpr(opcode[3..0].asInt)
    val imm = ARMImmediate(opcode[11..7], false)
    private val shiftType = find<ShiftType> { it.id == opcode[6..5] }

    override fun toString(): String = "$rm, $shiftType $imm"

    override fun equals(other: Any?): Boolean =
            other is ARMImmediateShift &&
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

    override fun value(core: AARMCore): Long {
        val data = imm.value.asInt
        return when (shiftType) {
            ShiftType.LSL -> if (data <= 0L) rm.value(core) else (rm.value(core) shl data) mask 32
            ShiftType.LSR -> if (data <= 0L) rm.value(core) else rm.value(core) ushr data
            ShiftType.ASR -> if (data <= 0L) if (carry()) 0xFFFFFFFF else 0 else rm.value(core) shr imm.value.asInt
            ShiftType.ROR -> if (data <= 0L) (rm.value(core) ushr 1) or (core.cpu.flags.c.asLong shl 31) else rm.value(core) rotr32 data
            ShiftType.RRX -> RRX(rm.value(core), 32, core.cpu.flags.c.asInt)
            else -> throw IllegalStateException("Unexpected shift type!")
        }
    }

    override fun carry(): Boolean {
        val data = imm.value.asInt
        return when (shiftType) {
            ShiftType.LSL -> if (data <= 0L) cpu.cpu.flags.c else rm.value(cpu)[32 - data].toBool()
            ShiftType.LSR, ShiftType.ASR -> if (data <= 0L) rm.value(cpu)[31].toBool() else rm.value(cpu)[data - 1].toBool()
            ShiftType.ROR -> if (data <= 0L) rm.value(cpu)[0].toBool() else rm.value(cpu)[data - 1].toBool()
            else -> throw IllegalStateException("Unexpected shift type!")
        }
    }
}