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
package ru.inforion.lab403.kopycat.cores.msp430.flags

import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.cores.msp430.MSP430Operand
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core



@Suppress("NOTHING_TO_INLINE")
object FlagProcessor {

    inline fun processShiftFlag(core: MSP430Core, result : Variable<MSP430Core>, carry : Boolean) {
        core.cpu.flags.n = result.isNegative(core)
        core.cpu.flags.z = result.isZero(core)
        core.cpu.flags.c = carry
        core.cpu.flags.v = false
    }

    inline fun processLogicalFlag(core: MSP430Core, result : Variable<MSP430Core>) {
        core.cpu.flags.n = result.msb(core).toBool()
        core.cpu.flags.z = result.isZero(core)
        core.cpu.flags.c = result.isNotZero(core)
        core.cpu.flags.v = false
    }

    inline fun processXorFlag(core: MSP430Core, result : Variable<MSP430Core>, op1 : MSP430Operand, op2 : MSP430Operand) {
        core.cpu.flags.n = result.msb(core).toBool()
        core.cpu.flags.z = result.isZero(core)
        core.cpu.flags.c = result.isNotZero(core)
        core.cpu.flags.v = op1.isNegative(core) and op2.isNegative(core)
    }

    inline fun processArithmFlag(core: MSP430Core, result : Variable<MSP430Core>, op1 : MSP430Operand, op2 : MSP430Operand, isSubtract: Boolean) {
        core.cpu.flags.n = result.isNegative(core)
        core.cpu.flags.z = result.isZero(core)
        core.cpu.flags.c = result.isCarry(core)
        core.cpu.flags.v = result.isIntegerOverflow(core, op1, op2, isSubtract)
    }

    inline fun processDaddFlag(core: MSP430Core, result : Variable<MSP430Core>, carry : Boolean) {
        core.cpu.flags.n = result.msb(core).toBool()
        core.cpu.flags.z = result.isZero(core)
        core.cpu.flags.c = carry
    }

    inline fun processSxtFlag(core: MSP430Core, result : Variable<MSP430Core>) {
        core.cpu.flags.n = result.isNegative(core)
        core.cpu.flags.z = result.isZero(core)
        core.cpu.flags.c = result.isNotZero(core)
        core.cpu.flags.v = false
    }
}