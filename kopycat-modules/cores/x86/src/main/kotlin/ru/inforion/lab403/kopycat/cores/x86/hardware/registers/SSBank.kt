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
package ru.inforion.lab403.kopycat.cores.x86.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.x86.enums.SSR
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class SSBank(core: x86Core) : ARegistersBank<x86Core, SSR>(core, SSR.values(), bits = 32) {
    override val name: String = "Segment Selector Registers"

    var cs by valueOf(x86Register.SSR.cs)
    var ds by valueOf(x86Register.SSR.ds)
    var ss by valueOf(x86Register.SSR.ss)
    var es by valueOf(x86Register.SSR.es)
    var fs by valueOf(x86Register.SSR.fs)
    var gs by valueOf(x86Register.SSR.gs)

    override fun reset() {
        super.reset()
        cs = 0xFFFF000
        ds = 0x0000000  // perhaps must be cs = 0xFFFF000
    }
}