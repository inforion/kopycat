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
package ru.inforion.lab403.kopycat.cores.mips.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.mips.enums.eGPR
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


class GPRBank(core: MipsCore) : ARegistersBank<MipsCore, eGPR>(core, eGPR.values(), bits = 32) {
    override val name: String = "CPU General Purpose Registers"

    val zero by valueOf(GPR.zero)

    var at by valueOf(GPR.at)

    var a0 by valueOf(GPR.a0)
    var a1 by valueOf(GPR.a1)
    var a2 by valueOf(GPR.a2)
    var a3 by valueOf(GPR.a3)

    var v0 by valueOf(GPR.v0)
    var v1 by valueOf(GPR.v1)

    var k0 by valueOf(GPR.k0)
    var k1 by valueOf(GPR.k1)

    var t0 by valueOf(GPR.t0)
    var t1 by valueOf(GPR.t1)
    var t2 by valueOf(GPR.t2)
    var t3 by valueOf(GPR.t3)
    var t4 by valueOf(GPR.t4)
    var t5 by valueOf(GPR.t5)
    var t6 by valueOf(GPR.t6)
    var t7 by valueOf(GPR.t7)
    var t8 by valueOf(GPR.t8)
    var t9 by valueOf(GPR.t9)

    var s0 by valueOf(GPR.s0)
    var s1 by valueOf(GPR.s1)
    var s2 by valueOf(GPR.s2)
    var s3 by valueOf(GPR.s3)
    var s4 by valueOf(GPR.s4)
    var s5 by valueOf(GPR.s5)
    var s6 by valueOf(GPR.s6)
    var s7 by valueOf(GPR.s7)
    
    var ra by valueOf(GPR.ra)
    var gp by valueOf(GPR.gp)
    var fp by valueOf(GPR.fp)
    var sp by valueOf(GPR.sp)

}
