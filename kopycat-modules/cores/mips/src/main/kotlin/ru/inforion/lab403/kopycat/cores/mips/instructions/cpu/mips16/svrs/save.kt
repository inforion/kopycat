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

import ru.inforion.lab403.common.extensions.minus
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction16
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import ru.inforion.lab403.kopycat.interfaces.*

/**
 * Created by shiftdj on 18.06.2021.
 */

class save(
        core: MipsCore,
        data: ULong,
        val s: Boolean,
        val ra: Boolean,
        val s0: Boolean,
        val s1: Boolean,
        val framesize: Int) : AMipsInstruction16(core, data, Type.VOID)  {

    override val mnem = "save"

    override fun execute() {
        var temp = core.reg(29)
        if (ra) {
            temp -= 4u
            core.outl(temp, core.reg(31))
        }
        if (s1) {
            temp -= 4u
            core.outl(temp, core.reg(17))
        }
        if (s0) {
            temp -= 4u
            core.outl(temp, core.reg(16))
        }
        temp = if (framesize == 0)
            core.reg(29) - 128u
        else
            core.reg(29) - (framesize shl 3)
        core.reg(29, temp)
    }
}