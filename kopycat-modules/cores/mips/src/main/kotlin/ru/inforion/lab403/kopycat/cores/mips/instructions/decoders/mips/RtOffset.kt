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
package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders.mips

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.common.extensions.signextRenameMeAfter
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.ProcType
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.instructions.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * LB, LBU, LDC1(rt=ft), LDC2, LH, LHU, LW, LWC1(rt=ft), LWC2, LWL, LWR, LL, SB, SC,
 * SDC1(rt=ft), SDC2, SH, SW, SWC1(rt=ft), SWC2, SWL, SWR
 */

class RtOffset(
        core: MipsCore,
        val construct: (MipsCore, ULong, MipsRegister, MipsDisplacement) -> AMipsInstruction,
        val dtyp: Datatype,
        val store: AccessAction,
        val type: ProcType = ProcType.CentralProc
) : ADecoder(core) {

    override fun decode(data: ULong): AMipsInstruction {
        val rt = data[20..16].int
        val offset = data[15..0].signextRenameMeAfter(15)
        val base = data[25..21].int
        return construct(core, data, gpr(rt), displ(dtyp, base, offset))
    }
}
