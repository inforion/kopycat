/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
package ru.inforion.lab403.kopycat.modules.msp430x44x

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException



class HardwareMultiplier(parent: Module, name: String) : Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val mem = Port("mem")
    }

    override val ports = Ports()

    var mode = 0

    //Unsigned multiplication
    val MPY = object : Register(ports.mem, 0x130u, WORD, "MPY") {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            mode = 0
        }
    }

    //Signed multiplication
    val MPYS = object : Register(ports.mem, 0x132u, WORD, "MPYS") {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            mode = 1
        }
    }

    //Unsigned multiply-and-accumulate
    val MAC = object : Register(ports.mem, 0x134u, WORD, "MAC") {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            mode = 2
        }
    }

    //Second operand
    val OP2 = object : Register(ports.mem, 0x138u, WORD, "OP2") {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            when(mode) {
                0 -> {
                    val result = MPY.data * data
                    RESLO.data = result[15..0]
                    RESHI.data = result[31..16]
                    SUMEXT.data = 0u
                }
                1 -> {
                    var sign = false

                    val op1Signed = MPY.data.long.signextRenameMeAfter(15)
                    val op1Unsigned = when {
                        op1Signed < 0 -> {
                            sign = true
                            -op1Signed
                        }
                        else -> op1Signed
                    }

                    val op2Signed = data.long.signextRenameMeAfter(15)
                    val op2Unsigned = when {
                        op2Signed < 0 -> {
                            sign = !sign
                            -op2Signed
                        }
                        else -> op2Signed
                    }

                    val resultUnsigned = op1Unsigned * op2Unsigned
                    val resultSigned = if (sign) -resultUnsigned else resultUnsigned

                    RESLO.data = resultSigned[15..0].ulong
                    RESHI.data = resultSigned[31..16].ulong
                    SUMEXT.data = resultSigned[47..32].ulong
                }
                2 -> throw GeneralException("MAC mode isn't implemented in HardwareMultiplier.kt")
                else -> throw GeneralException("Unknown mode in HardwareMultiplier.kt")
            }
        }

    }

    //Result low word
    val RESLO = Register(ports.mem, 0x13Au, WORD, "RESLO")

    //Result high word
    val RESHI = Register(ports.mem, 0x13Cu, WORD, "RESHI")

    //Sum extend
    val SUMEXT = object : Register(ports.mem, 0x13Eu, WORD, "SUMEXT") {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong): Unit =
            throw GeneralException("try to write in SUMEXT that is read-only register")
    }
}