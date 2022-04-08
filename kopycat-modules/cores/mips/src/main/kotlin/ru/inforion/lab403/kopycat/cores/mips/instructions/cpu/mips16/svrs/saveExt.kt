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
package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.mips16.svrs

import ru.inforion.lab403.common.extensions.hex2
import ru.inforion.lab403.common.extensions.minus
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import ru.inforion.lab403.kopycat.interfaces.*

/**
 * Created by shiftdj on 18.06.2021.
 */

class saveExt(
        core: MipsCore,
        data: ULong,
        val xregs: Int,
        val framesize: Int,
        val aregs: Int,
        val s: Boolean,
        val ra: Boolean,
        val s0: Boolean,
        val s1: Boolean) : AMipsInstruction(core, data, Type.VOID)  {

    override val mnem = "save"

    override fun execute() {
        var temp = core.reg(29)
        val temp2 = temp
        val args = when (aregs) {
            0b0000, 0b0001, 0b0010, 0b0011, 0b1011 -> 0
            0b0100, 0b0101, 0b0110, 0b0111 -> 1
            0b1000, 0b1001, 0b1010 -> 2
            0b1100, 0b1101 -> 3
            0b1110 -> 4
            else -> throw GeneralException("Unknown aregs value: ${aregs.hex2}")
        }
        if (args > 0) {
            core.outl(temp, core.reg(4))
            if (args > 1) {
                core.outl(temp + 4u, core.reg(5))
                if (args > 2) {
                    core.outl(temp + 8u, core.reg(6))
                    if (args > 3)
                        core.outl(temp + 12u, core.reg(7))
                }
            }
        }
        if (ra) {
            temp -= 4u
            core.outl(temp, core.reg(31))
        }
        if (xregs > 0) {
            if (xregs > 1) {
                if (xregs > 2) {
                    if (xregs > 3) {
                        if (xregs > 4) {
                            if (xregs > 5) {
                                if (xregs > 6) {
                                    temp -= 4u
                                    core.outl(temp, core.reg(30))
                                }
                                temp -= 4u
                                core.outl(temp, core.reg(23))
                            }
                            temp -= 4u
                            core.outl(temp, core.reg(22))
                        }
                        temp -= 4u
                        core.outl(temp, core.reg(21))
                    }
                    temp -= 4u
                    core.outl(temp, core.reg(20))
                }
                temp -= 4u
                core.outl(temp, core.reg(19))
            }
            temp -= 4u
            core.outl(temp, core.reg(18))
        }
        if (s1) {
            temp -= 4u
            core.outl(temp, core.reg(17))
        }
        if (s0) {
            temp -= 4u
            core.outl(temp, core.reg(16))
        }
        val astatic = when (aregs) {
            0b0000, 0b0100, 0b1000, 0b1100, 0b1110 -> 0
            0b0001, 0b0101, 0b1001, 0b1101 -> 1
            0b0010, 0b0110, 0b1010, -> 2
            0b0011, 0b0111 -> 3
            0b1011 -> 4
            else -> throw GeneralException("Unknown aregs value: ${aregs.hex2}")
        }
        if (astatic > 0) {
            temp -= 4u
            core.outl(temp, core.reg(7))
            if (astatic > 1) {
                temp -= 4u
                core.outl(temp, core.reg(6))
                if (astatic > 2) {
                    temp -= 4u
                    core.outl(temp, core.reg(5))
                    if (astatic > 3) {
                        temp -= 4u
                        core.outl(temp, core.reg(4))
                    }
                }
            }
        }
        temp = temp2 - (framesize shl 3)
        core.reg(29, temp)
    }
}