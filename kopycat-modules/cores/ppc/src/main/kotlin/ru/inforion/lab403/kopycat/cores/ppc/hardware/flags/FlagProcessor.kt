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
package ru.inforion.lab403.kopycat.cores.ppc.flags

import ru.inforion.lab403.common.extensions.mask
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.modules.cores.PPCCore


@Suppress("NOTHING_TO_INLINE")
object FlagProcessor {

    inline fun processCR0(core: PPCCore, result : Variable<PPCCore>) {
        core.cpu.crBits.CR0.field = 0
        when {
            result.isZero(core) -> core.cpu.crBits.CR0.EQ = true
            result.isNegative(core) -> core.cpu.crBits.CR0.LT = true
            else -> core.cpu.crBits.CR0.GT = true
        }
        core.cpu.crBits.CR0.SO = core.cpu.xerBits.SO
    }

    inline fun processCarry(core: PPCCore, result: Variable<PPCCore>) {
        core.cpu.xerBits.CA = result.isCarry(core)
    }

    inline fun processCarryAlgShift(core: PPCCore, data: Variable<PPCCore>, n: Int) {
        core.cpu.xerBits.CA = data.isCarry(core) && (data.value(core) mask n != 0L) //Won't work on 64 bit system
    }

    inline fun processOverflow(core: PPCCore, result: Variable<PPCCore>) {
        //result.isOverflow() - no works
        core.cpu.xerBits.OV = result.bit(core, result.msb(core)).toBool() xor result.isCarry(core)
        if (core.cpu.xerBits.OV)
            core.cpu.xerBits.SO = true
    }

    inline fun processOverflowDiv(core: PPCCore, ovr: Boolean) {
        core.cpu.xerBits.OV = ovr
    }

}