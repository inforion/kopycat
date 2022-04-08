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
import ru.inforion.lab403.common.extensions.plus
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import ru.inforion.lab403.kopycat.interfaces.*

/**
 * Created by shiftdj on 18.06.2021.
 */

class restoreExt(
        core: MipsCore,
        data: ULong,
        val xregs: Int,
        val framesize: Int,
        val aregs: Int,
        val s: Boolean,
        val ra: Boolean,
        val s0: Boolean,
        val s1: Boolean) : AMipsInstruction(core, data, Type.VOID)  {

    override val mnem = "restore"

    override fun execute() {
        var temp = core.reg(29) + (framesize shl 3)
        val temp2 = temp
        if (ra) {
            temp -= 4u
            core.reg(31, core.inl(temp))
        }
        if (xregs > 0) {
            if (xregs > 1) {
                if (xregs > 2) {
                    if (xregs > 3) {
                        if (xregs > 4) {
                            if (xregs > 5) {
                                if (xregs > 6) {
                                    temp -= 4u
                                    core.reg(30, core.inl(temp))
                                }
                                temp -= 4u
                                core.reg(23, core.inl(temp))
                            }
                            temp -= 4u
                            core.reg(22, core.inl(temp))
                        }
                        temp -= 4u
                        core.reg(21, core.inl(temp))
                    }
                    temp -= 4u
                    core.reg(20, core.inl(temp))
                }
                temp -= 4u
                core.reg(19, core.inl(temp))
            }
            temp -= 4u
            core.reg(18, core.inl(temp))
        }
        if (s1) {
            temp -= 4u
            core.reg(17, core.inl(temp))
        }
        if (s0) {
            temp -= 4u
            core.reg(16, core.inl(temp))
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
            core.reg(7, core.inl(temp))
            if (astatic > 1) {
                temp -= 4u
                core.reg(6, core.inl(temp))
                if (astatic > 2) {
                    temp -= 4u
                    core.reg(5, core.inl(temp))
                    if (astatic > 3) {
                        temp -= 4u
                        core.reg(4, core.inl(temp))
                    }
                }
            }
        }
        core.reg(29, temp2)
    }
}