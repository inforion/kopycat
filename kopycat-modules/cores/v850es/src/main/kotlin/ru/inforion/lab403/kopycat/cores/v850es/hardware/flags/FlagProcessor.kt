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
package ru.inforion.lab403.kopycat.cores.v850es.hardware.flags

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.cores.v850es.v850ESOperand
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



@Suppress("NOTHING_TO_INLINE")
object FlagProcessor {

    inline fun processArithmFlag(core: v850ESCore, result: Variable<v850ESCore>, op1: v850ESOperand, op2: v850ESOperand, isSubtract: Boolean) {
        core.cpu.flags.cy = result.isCarry(core)
        core.cpu.flags.ov = result.isIntegerOverflow(core, op1, op2, isSubtract)
        core.cpu.flags.z = result.isZero(core)
        core.cpu.flags.s = result.isNegative(core)
    }

    inline fun processLogicalFlag(core: v850ESCore, result: Variable<v850ESCore>) {
        core.cpu.flags.ov = false
        core.cpu.flags.z = result.isZero(core)
        core.cpu.flags.s = result.isNegative(core)
    }

    inline fun processDivFlag(core: v850ESCore, result: Variable<v850ESCore>, op1: v850ESOperand, op2: v850ESOperand) {
        core.cpu.flags.ov = result.isIntegerOverflow(core, op1, op2, false)
        core.cpu.flags.z = result.isZero(core)
        core.cpu.flags.s = result.isNegative(core)
    }

    inline fun processSwapFlag(core: v850ESCore, result: Variable<v850ESCore>, cy: (Variable<v850ESCore>) -> Boolean) {
        core.cpu.flags.cy = cy(result)
        core.cpu.flags.ov = false
        core.cpu.flags.z = result.isZero(core)
        core.cpu.flags.s = result.isNegative(core)
    }

//    TODO: Implement saturation instructions

    inline fun processShiftFlag(
            core: v850ESCore, result: Variable<v850ESCore>, op1: v850ESOperand, op2: v850ESOperand,
            lastShiftedBitIndex : () -> Int) {
        // TODO: SAR - not implemented -> tested only for logical shift
        // 1 if the bit shifted out last is 1; otherwise, 0. However, if the number of shifts is 0, the result is 0.
        val index = lastShiftedBitIndex()
        core.cpu.flags.cy = if (index >= 0) op2.bit(core, index) == 1 else false
        core.cpu.flags.ov = false
        core.cpu.flags.z = result.isZero(core)
        core.cpu.flags.s = result.isNegative(core)
    }

    inline fun processBitManFlag(core: v850ESCore, result: Variable<v850ESCore>, op1: v850ESOperand, op2: v850ESOperand) {
        core.cpu.flags.z = result.bit(core, op2.bits(core,2..0).asInt) == 0
    }
}