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
import ru.inforion.lab403.kopycat.cores.x86.enums.SWR
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class SWRBank(core: x86Core) : ARegistersBank<x86Core, SWR>(core, SWR.values(), bits = 16) {
    override val name: String = "SWR Register"

    var ie by bitOf(x86Register.FWR.SWR, SWR.IE.bit)
    var de by bitOf(x86Register.FWR.SWR, SWR.DE.bit)
    var xe by bitOf(x86Register.FWR.SWR, SWR.XE.bit)
    var oe by bitOf(x86Register.FWR.SWR, SWR.OE.bit)
    var ue by bitOf(x86Register.FWR.SWR, SWR.UE.bit)
    var pe by bitOf(x86Register.FWR.SWR, SWR.PE.bit)
    var sf by bitOf(x86Register.FWR.SWR, SWR.SF.bit)
    var es by bitOf(x86Register.FWR.SWR, SWR.ES.bit)
    var c0 by bitOf(x86Register.FWR.SWR, SWR.C0.bit)
    var c1 by bitOf(x86Register.FWR.SWR, SWR.C1.bit)
    var c2 by bitOf(x86Register.FWR.SWR, SWR.C2.bit)
    var top by bitOf(x86Register.FWR.SWR, SWR.TOP.bit)
    var c3 by bitOf(x86Register.FWR.SWR, SWR.C3.bit)
    var b by bitOf(x86Register.FWR.SWR, SWR.B.bit)
}