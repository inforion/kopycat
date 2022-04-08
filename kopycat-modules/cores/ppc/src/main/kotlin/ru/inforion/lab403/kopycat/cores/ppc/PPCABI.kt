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
package ru.inforion.lab403.kopycat.cores.ppc

import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.ppc.enums.eUISA
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore

class PPCABI(core: PPCCore, bigEndian: Boolean): ABI<PPCCore>(core, 32, bigEndian) {
        @DontAutoSerialize
        override val regArguments = listOf(
            eUISA.GPR3.id,
            eUISA.GPR4.id,
            eUISA.GPR5.id,
            eUISA.GPR6.id,
            eUISA.GPR7.id,
            eUISA.GPR8.id,
            eUISA.GPR9.id,
            eUISA.GPR10.id)

        override val minimumStackAlignment: Int = 16

        override val gprDatatype = Datatype.DWORD
        override fun register(index: Int) = PPCRegister.uisa(index)
        override val registerCount: Int get() = core.cpu.count()
        override val sizetDatatype = Datatype.DWORD

        override fun createContext() = PPCContext(this)

        override val pc get() = PPCRegister.UISA.PC
        override val sp get() = PPCRegister.UISA.GPR1
        override val ra get() = PPCRegister.UISA.LR
        override val rv get() = PPCRegister.UISA.GPR3
    }