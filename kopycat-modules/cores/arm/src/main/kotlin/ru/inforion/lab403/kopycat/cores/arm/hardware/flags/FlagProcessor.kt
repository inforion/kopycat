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
package ru.inforion.lab403.kopycat.cores.arm.hardware.flags

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.SInt
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



@Suppress("NOTHING_TO_INLINE")
object FlagProcessor {
    inline fun processArithmFlag(core: AARMCore, result: Long, carry: Int, overflow: Int) {
        core.cpu.flags.n = result[31] == 1L
        core.cpu.flags.z = result == 0L
        core.cpu.flags.c = carry == 1
        core.cpu.flags.v = overflow == 1
    }

    inline fun processLogicFlag(core: AARMCore, result: Variable<AARMCore>, shifterCarryOut: Boolean) {
        core.cpu.flags.n = result.isNegative(core)
        core.cpu.flags.z = result.isZero(core)
        core.cpu.flags.c = shifterCarryOut
    }

    inline fun processMulFlag(core: AARMCore, result: Variable<AARMCore>) {
        core.cpu.flags.n = result.isNegative(core)
        core.cpu.flags.z = result.isZero(core)
    }

    inline fun processHMulFlag(core: AARMCore, result: Long) {
        core.cpu.status.q = result != SInt(result[31..0], 32)
    }

    inline fun processHMulRegFlag(core: AARMCore, result: Long, rd: ARMRegister) {
        core.cpu.status.q = result.shr(16) != rd.ssext(core)
    }

    inline fun processSatFlag(core: AARMCore) {
        core.cpu.status.q = true
    }
}