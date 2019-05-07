package ru.inforion.lab403.kopycat.cores.arm.operands

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.arm.enums.ShiftType
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Type.CUSTOM
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.cores.arm.enums.GPR as eGPR

/**
 * Created by the bat on 13.01.18.
 */

class ARMRegisterShift(cpu: AARMCore, opcode: Long) : AARMShift(cpu, opcode) {

    val rs = ARMRegister.gpr(opcode[11..8].asInt)
    val rm = ARMRegister.gpr(opcode[3..0].asInt)
    private val shiftType = find<ShiftType> { it.id == opcode[6..5] }

    override fun toString(): String = "$rm, $shiftType $rs"

    override fun equals(other: Any?): Boolean =
            other is ARMRegisterShift &&
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
        val shifter = (rs.value(core) and 0xFF).asInt

        return when (shiftType) {
            ShiftType.LSL -> when {
                shifter <= 0 -> rm.value(core)
                shifter < 32 -> rm.value(core) shl shifter
                else -> 0
            }
            ShiftType.LSR -> when {
                shifter <= 0 -> rm.value(core)
                shifter < 32 -> rm.value(core) ushr shifter
                else -> 0
            }
            ShiftType.ASR -> when {
                shifter <= 0 -> rm.value(core)
                shifter < 32 -> rm.value(core) shr shifter
                shifter == 32 -> 0
                else -> 0xFFFFFFFF
            }
            ShiftType.ROR -> when {
                shifter <= 0 -> rm.value(core)
                (shifter and 0b11111) == 0 -> rm.value(core)
                (shifter and 0b11111) > 0 -> rm.value(core) rotr32 (shifter and 0b11111)
                else -> throw IllegalStateException("Unexpected shifter value!")
            }
            else -> throw IllegalStateException("Unexpected shift type!")
        }
    }

    override fun carry(): Boolean{
        val shifter = (rs.value(cpu) and 0xFF).asInt
        return when (shiftType) {
            ShiftType.LSL -> when {
                shifter <= 0 -> cpu.cpu.flags.c
                shifter < 32 -> rm.value(cpu)[32 - shifter].toBool()
                shifter == 32 -> rm.value(cpu)[0].toBool()
                else -> false
            }
            ShiftType.LSR -> when {
                shifter <= 0 -> cpu.cpu.flags.c
                shifter < 32 -> rm.value(cpu)[shifter - 1].toBool()
                shifter == 32 -> rm.value(cpu)[31].toBool()
                else -> false
            }
            ShiftType.ASR -> when {
                shifter <= 0L -> cpu.cpu.flags.c
                shifter < 32L -> rm.value(cpu)[shifter - 1].toBool()
                else -> rm.value(cpu)[31].toBool()
            }
            ShiftType.ROR -> when {
                shifter <= 0L -> cpu.cpu.flags.c
                (shifter and 0b11111) == 0 -> rm.value(cpu)[31].toBool()
                (shifter and 0b11111) > 0 -> rm.value(cpu)[(shifter and 0b11111) - 1].toBool()
                else -> throw IllegalStateException("Unexpected shifter value!")
            }
            else -> throw IllegalStateException("Unexpected shift type!")
        }
    }
}