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
package ru.inforion.lab403.kopycat.cores.x86.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.x86.enums.DBGR
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class DBGBank : ARegistersBankNG<x86Core>("Debug Registers", DBGR.values().size, bits = 16) {

    val dr0 = Register("dr0",0, dtype = Datatype.WORD)
    val dr1 = Register("dr1",1, dtype = Datatype.WORD)
    val dr2 = Register("dr2",2, dtype = Datatype.WORD)
    val dr3 = Register("dr3",3, dtype = Datatype.WORD)
    val dr4 = Register("dr4",4, dtype = Datatype.WORD)
    val dr5 = Register("dr5",5, dtype = Datatype.WORD)
    val dr6 = Register("dr6",6, dtype = Datatype.WORD)
    val dr7 = Register("dr7",7, dtype = Datatype.WORD)
}