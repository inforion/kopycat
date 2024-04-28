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
package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.mips16.svrs

import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction16
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import ru.inforion.lab403.common.extensions.plus
import ru.inforion.lab403.kopycat.interfaces.*

/**
 * Created by shiftdj on 18.06.2021.
 */

// RESTORE {ra,}{s0/s1/s0-1,}{framesize} (All args are optional)
class restore(
        core: MipsCore,
        data: ULong,
        val s: Boolean,
        val ra: Boolean,
        val s0: Boolean,
        val s1: Boolean,
        val framesize: Int) : AMipsInstruction16(core, data, Type.VOID)  {

    override val mnem = "restore"

    override fun execute() {
        var temp = when (framesize) {
            0 -> core.reg(29) + 128u
            else -> core.reg(29) + (framesize shl 3)
        }

        val temp2 = temp

        if (ra) {
            temp -= 4u
            core.reg(31, core.inl(temp))
        }

        if (s1) {
            temp -= 4u
            core.reg(17, core.inl(temp))
        }

        if (s0) {
            temp -= 4u
            core.reg(16, core.inl(temp))
        }
        core.reg(29, temp2)
    }
}