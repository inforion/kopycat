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
import ru.inforion.lab403.kopycat.cores.mips.enums.eCPR
import ru.inforion.lab403.kopycat.cores.mips.operands.CPR
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


class CPRBank(core: MipsCore) : ARegistersBank<MipsCore, eCPR>(core, eCPR.values(), bits = 32) {
    override val name: String = "Coprocessor General Purpose Registers"

    var Index by valueOf(CPR.Index)
    var Config0 by valueOf(CPR.Config0)
    var Config1 by valueOf(CPR.Config1)
    var Config2 by valueOf(CPR.Config2)
    var Config3 by valueOf(CPR.Config3)
    var Cause by valueOf(CPR.Cause)
    var Context by valueOf(CPR.Context)
    var UserLocal by valueOf(CPR.UserLocal)
    var Compare by valueOf(CPR.Compare)
    var PageMask by valueOf(CPR.PageMask)
    var EntryHi by valueOf(CPR.EntryHi)
    var EntryLo0 by valueOf(CPR.EntryLo0)
    var EntryLo1 by valueOf(CPR.EntryLo1)
    var Count by valueOf(CPR.Count)
    var EPC by valueOf(CPR.EPC)
    var ErrorEPC by valueOf(CPR.ErrorEPC)
    var PRId by valueOf(CPR.PRId)
    var Random by valueOf(CPR.Random)
    var Wired by valueOf(CPR.Wired)
    var Status by valueOf(CPR.Status)
    var ECC by valueOf(CPR.ErrCnt)
    var TagLo0 by valueOf(CPR.TagLo0)
    var TagLo2 by valueOf(CPR.TagLo2)
    var BadVAddr by valueOf(CPR.BadVAddr)
    var IntCtl by valueOf(CPR.IntCtl)
    var SRSCtl by valueOf(CPR.SRSCtl)
    var SRSMap by valueOf(CPR.SRSMap)
    var EBase by valueOf(CPR.EBase)
}
