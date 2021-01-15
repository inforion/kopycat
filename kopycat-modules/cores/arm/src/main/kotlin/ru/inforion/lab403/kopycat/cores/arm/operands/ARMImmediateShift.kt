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

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.arm.RRX
import ru.inforion.lab403.kopycat.cores.arm.enums.ShiftType
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


class ARMImmediateShift constructor(val rm: ARMRegister, val imm: ARMImmediate, val shift: ShiftType) : AARMShift() {

    override fun toString(): String = "$rm, $shift $imm"

    override fun value(core: AARMCore): Long {
        val data = imm.value.asInt
        return when (shift) {
            ShiftType.LSL -> if (data <= 0L) rm.value(core) else (rm.value(core) shl data) mask 32
            ShiftType.LSR -> if (data <= 0L) rm.value(core) else rm.value(core) ushr data
            ShiftType.ASR -> if (data <= 0L) if (carry(core)) 0xFFFFFFFF else 0 else rm.value(core) shr imm.value.asInt
            ShiftType.ROR -> if (data <= 0L) (rm.value(core) ushr 1) or (core.cpu.flags.c.asLong shl 31) else rm.value(core) rotr32 data
            ShiftType.RRX -> RRX(rm.value(core), 32, core.cpu.flags.c.asInt)
            else -> throw IllegalStateException("Unexpected shift type!")
        }
    }

    override fun carry(core: AARMCore): Boolean {
        val data = imm.value.asInt
        return when (shift) {
            ShiftType.LSL -> if (data <= 0L) core.cpu.flags.c else rm.value(core)[32 - data].toBool()
            ShiftType.LSR, ShiftType.ASR -> if (data <= 0L) rm.value(core)[31].toBool() else rm.value(core)[data - 1].toBool()
            ShiftType.ROR -> if (data <= 0L) rm.value(core)[0].toBool() else rm.value(core)[data - 1].toBool()
            else -> throw IllegalStateException("Unexpected shift type!")
        }
    }
}