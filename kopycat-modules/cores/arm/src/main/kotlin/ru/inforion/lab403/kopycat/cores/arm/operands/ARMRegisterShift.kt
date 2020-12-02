/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Non-free licenses may also be purchased from INFORION, LLC, 
 * for users who do not want their programs protected by the GPL. 
 * Contact us for details kopycat@inforion.ru
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package ru.inforion.lab403.kopycat.cores.arm.operands

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.rotr32
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.arm.enums.ShiftType
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


class ARMRegisterShift constructor(val rs: ARMRegister, val rm: ARMRegister, val shift: ShiftType) : AARMShift() {

    override fun toString(): String = "$rm, $shift $rs"

    override fun value(core: AARMCore): Long {
        val shifter = (rs.value(core) and 0xFF).asInt

        return when (shift) {
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

    override fun carry(core: AARMCore): Boolean{
        val shifter = (rs.value(core) and 0xFF).asInt
        return when (shift) {
            ShiftType.LSL -> when {
                shifter <= 0 -> core.cpu.flags.c
                shifter < 32 -> rm.value(core)[32 - shifter].toBool()
                shifter == 32 -> rm.value(core)[0].toBool()
                else -> false
            }
            ShiftType.LSR -> when {
                shifter <= 0 -> core.cpu.flags.c
                shifter < 32 -> rm.value(core)[shifter - 1].toBool()
                shifter == 32 -> rm.value(core)[31].toBool()
                else -> false
            }
            ShiftType.ASR -> when {
                shifter <= 0L -> core.cpu.flags.c
                shifter < 32L -> rm.value(core)[shifter - 1].toBool()
                else -> rm.value(core)[31].toBool()
            }
            ShiftType.ROR -> when {
                shifter <= 0L -> core.cpu.flags.c
                (shifter and 0b11111) == 0 -> rm.value(core)[31].toBool()
                (shifter and 0b11111) > 0 -> rm.value(core)[(shifter and 0b11111) - 1].toBool()
                else -> throw IllegalStateException("Unexpected shifter value!")
            }
            else -> throw IllegalStateException("Unexpected shift type!")
        }
    }
}