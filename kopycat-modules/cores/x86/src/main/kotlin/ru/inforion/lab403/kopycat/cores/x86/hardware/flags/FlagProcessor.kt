/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.kopycat.cores.x86.hardware.flags

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.ushr
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.modules.cores.x86Core

object FlagProcessor {

    //val delegate...
    fun getPF(core: x86Core, res: Variable<x86Core>): Boolean {
        var bitCounter = 0
        var tmp = res.value(core)
        for (i in 0..7) {
            if (tmp % 2uL != 0uL) bitCounter++
            tmp = tmp ushr 1
        }
        return bitCounter % 2 == 0
    }

    fun getAF(core: x86Core, result: Variable<x86Core>, op1: AOperand<x86Core>, op2: AOperand<x86Core>): Boolean {
        val a1 = result.value(core)
        val a2 = op1.value(core)
        val a3 = op2.value(core)

        return a1[4] xor a2[4] xor a3[4] != 0uL
    }

    fun processAsciiAdjustFlag(core: x86Core, isOvf: Boolean) =
            if(isOvf){
                core.cpu.flags.eflags.af = true
                core.cpu.flags.eflags.cf = true
            } else {
                core.cpu.flags.eflags.af = false
                core.cpu.flags.eflags.cf = false
            }


    /*
        The OF flag is defined only for the 1-bit rotates; it is undefined in all other cases (except that a
    zero-bit rotate does nothing, that is affects no flags). For left rotates, the OF flag is set to the
    exclusive OR of the CF bit (after the rotate) and the most-significant bit of the result. For right
    rotates, the OF flag is set to the exclusive OR of the two most-significant bits of the result.
    */
    fun getOFRotate(core: x86Core, result: Variable<x86Core>, isLeft: Boolean): Boolean =
            if (isLeft) core.cpu.flags.eflags.cf xor (result.value(core)[result.dtyp.bits - 1] == 1uL)
            else (result.value(core)[result.dtyp.bits - 1] == 1uL) xor (result.value(core)[result.dtyp.bits - 2] == 1uL)

    /*
        The CF flag contains the value of the last bit shifted out of the destination operand; it is unde-
    fined for SHL and SHR instructions where the count is greater than or equal to the size (in bits)
    of the destination operand. The OF flag is affected only for 1-bit shifts (refer to “Description”
    above); otherwise, it is undefined. The SF, ZF, and PF flags are set according to the result. If the
    count is 0, the flags are not affected. For a non-zero count, the AF flag is undefined.
        The OF flag is affected only on 1-bit shifts. For left shifts, the OF flag is cleared to 0 if the most-
    significant bit of the result is the same as the CF flag (that is, the top two bits of the original
    operand were the same); otherwise, it is set to 1. For the SAR instruction, the OF flag is cleared
    for all 1-bit shifts. For the SHR instruction, the OF flag is set to the most-significant bit of the
    original operand.
    */
    fun getOFShift(core: x86Core, result: Variable<x86Core>, op1: AOperand<x86Core>, op2: AOperand<x86Core>,
                   isLeft: Boolean, isSar: Boolean): Boolean =
            if(op2.value(core) == 1uL)
                if(isSar)
                    false
                else if(isLeft)
                    core.cpu.flags.eflags.cf != (result.value(core)[result.dtyp.bits - 1] == 1uL)
                else
                    op1.value(core)[op1.dtyp.bits - 1] == 1uL
            else
                false

    fun processOneOpImulFlag() {}
    fun processTwoThreeOpImulFlag() {}
    fun processCliFlag() {}

    fun processAddSubCmpFlag(core: x86Core, result: Variable<x86Core>, op1: AOperand<x86Core>, op2: AOperand<x86Core>, isSubtract: Boolean) {
        core.cpu.flags.eflags.zf = result.isZero(core)
        core.cpu.flags.eflags.sf = result.isNegative(core)
        core.cpu.flags.eflags.cf = result.isCarry(core, op1, op2, isSubtract)
        core.cpu.flags.eflags.of = result.isIntegerOverflow(core, op1, op2, isSubtract)
        core.cpu.flags.eflags.pf = getPF(core, result)
        core.cpu.flags.eflags.af = getAF(core, result, op1, op2)
    }

    fun processNegFlag(core: x86Core, result: Variable<x86Core>) {
        core.cpu.flags.eflags.cf = result.value(core) != 0uL
    }

    fun processIncDecFlag(core: x86Core, result: Variable<x86Core>, op1: AOperand<x86Core>, op2: AOperand<x86Core>, isSubtract: Boolean){
        core.cpu.flags.eflags.pf = getPF(core, result)
        core.cpu.flags.eflags.zf = result.isZero(core)
        core.cpu.flags.eflags.sf = result.isNegative(core)
        core.cpu.flags.eflags.of = result.isIntegerOverflow(core, op1, op2, isSubtract)
        core.cpu.flags.eflags.af = getAF(core, result, op1, op2)
    }

    fun processRotateFlag(core: x86Core, result: Variable<x86Core>, op2: AOperand<x86Core>, isLeft: Boolean, cf: Boolean) {
        core.cpu.flags.eflags.cf = cf
        if(op2.value(core) == 1uL) core.cpu.flags.eflags.of = getOFRotate(core, result, isLeft)
    }

    fun processShiftFlag(core: x86Core, result: Variable<x86Core>, op1: AOperand<x86Core>, op2: AOperand<x86Core>,
                         isLeft: Boolean, isSar: Boolean, cf: Boolean) {
        core.cpu.flags.eflags.cf = cf
        core.cpu.flags.eflags.pf = getPF(core, result)
        core.cpu.flags.eflags.zf = result.isZero(core)
        core.cpu.flags.eflags.sf = result.isNegative(core)
        core.cpu.flags.eflags.of = getOFShift(core, result, op1, op2, isLeft, isSar)
    }

    fun processAndOrXorTestFlag(core: x86Core, result: Variable<x86Core>) {
        core.cpu.flags.eflags.zf = result.isZero(core)
        core.cpu.flags.eflags.sf = result.isNegative(core)
        core.cpu.flags.eflags.cf = false
        core.cpu.flags.eflags.of = false
        core.cpu.flags.eflags.pf = getPF(core, result)
        core.cpu.flags.eflags.af = false
        // undefined, but in bochs it is false
    }

    fun processMulFlag(core: x86Core, upperHalf: ULong) {
        if(upperHalf == 0uL){
            core.cpu.flags.eflags.of = false
            core.cpu.flags.eflags.cf = false
        } else {
            core.cpu.flags.eflags.of = true
            core.cpu.flags.eflags.cf = true
        }

    }




}