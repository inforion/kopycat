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
package ru.inforion.lab403.kopycat.cores.mips.operands

import ru.inforion.lab403.common.extensions.first
import ru.inforion.lab403.kopycat.cores.mips.enums.Designation
import ru.inforion.lab403.kopycat.cores.mips.enums.eFPR
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.ProcType
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

// FPU GPR
open class FPR(desc: eFPR) : MipsRegister<eFPR>(ProcType.FloatingPointCop, Designation.General, desc) {
    constructor(id: Int) : this(first<eFPR> { it.id == id })

    override fun value(core: MipsCore, data: Long) = core.fpu.regs.writeIntern(reg, data)
    override fun value(core: MipsCore): Long = core.fpu.regs.readIntern(reg)

    object f0 : FPR(eFPR.F0)
    object f1 : FPR(eFPR.F1)
    object f2 : FPR(eFPR.F2)
    object f3 : FPR(eFPR.F3)
    object f4 : FPR(eFPR.F4)
    object f5 : FPR(eFPR.F5)
    object f6 : FPR(eFPR.F6)
    object f7 : FPR(eFPR.F7)
    object f8 : FPR(eFPR.F8)
    object f9 : FPR(eFPR.F9)
    object f10 : FPR(eFPR.F10)
    object f11 : FPR(eFPR.F11)
    object f12 : FPR(eFPR.F12)
    object f13 : FPR(eFPR.F13)
    object f14 : FPR(eFPR.F14)
    object f15 : FPR(eFPR.F15)
    object f16 : FPR(eFPR.F16)
    object f17 : FPR(eFPR.F17)
    object f18 : FPR(eFPR.F18)
    object f19 : FPR(eFPR.F19)
    object f20 : FPR(eFPR.F20)
    object f21 : FPR(eFPR.F21)
    object f22 : FPR(eFPR.F22)
    object f23 : FPR(eFPR.F23)
    object f24 : FPR(eFPR.F24)
    object f25 : FPR(eFPR.F25)
    object f26 : FPR(eFPR.F26)
    object f27 : FPR(eFPR.F27)
    object f28 : FPR(eFPR.F28)
    object f29 : FPR(eFPR.F29)
    object f30 : FPR(eFPR.F30)
    object f31 : FPR(eFPR.F31)
}