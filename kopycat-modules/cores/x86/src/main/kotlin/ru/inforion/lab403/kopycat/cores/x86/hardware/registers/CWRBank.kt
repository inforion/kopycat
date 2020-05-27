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
import ru.inforion.lab403.kopycat.cores.x86.enums.CWR
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class CWRBank(core: x86Core) : ARegistersBank<x86Core, CWR>(core, CWR.values(), bits = 16) {
    override val name: String = "CWR Register"

    var i by bitOf(x86Register.FWR.CWR, CWR.I.bit)
    var d by bitOf(x86Register.FWR.CWR, CWR.D.bit)
    var z by bitOf(x86Register.FWR.CWR, CWR.Z.bit)
    var o by bitOf(x86Register.FWR.CWR, CWR.O.bit)
    var u by bitOf(x86Register.FWR.CWR, CWR.U.bit)
    var p by bitOf(x86Register.FWR.CWR, CWR.P.bit)
    var pc by bitOf(x86Register.FWR.CWR, CWR.PC.bit)
    var rc by bitOf(x86Register.FWR.CWR, CWR.RC.bit)
}