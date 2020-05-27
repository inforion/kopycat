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
package ru.inforion.lab403.kopycat.cores.mips

import ru.inforion.lab403.common.extensions.UNDEF
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.cores.mips.enums.eGPR
import ru.inforion.lab403.kopycat.cores.mips.operands.GPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore




class MIPSABI(core: MipsCore, heap: LongRange, stack: LongRange, bigEndian: Boolean):
        ABI<MipsCore>(core, heap, stack, bigEndian) {
    override fun gpr(index: Int): ARegister<MipsCore> = GPR(index)
    override fun createCpuContext() = MIPSContext(core.cpu)
    override val ssr = UNDEF
    override val sp = GPR(eGPR.SP.id)
    override val ra = GPR(eGPR.RA.id)
    override val v0 = GPR(eGPR.V0.id)
    override val argl = listOf(
            GPR(eGPR.A0.id),
            GPR(eGPR.A1.id),
            GPR(eGPR.A2.id),
            GPR(eGPR.A3.id),
            GPR(eGPR.T0.id),
            GPR(eGPR.T1.id))
}