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
package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders.mips16

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.cores.mips.enums.Designation
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.ProcType
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.instructions.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by shiftdj on 21.06.2021.
 */

class MOVR32_EXT(core: MipsCore,
                 val construct: (MipsCore, ULong, MipsRegister, MipsRegister) -> AMipsInstruction) : ADecoder(core) {

    override fun decode(data: ULong): AMipsInstruction {
        require(isExtended(data)) { "Can't be applied to not-extended instruction" }
        require(data[10..8] == 0uL) { "Not CPU0" }
        val lowData = getLowData(data)

        val sel = data[7..5].int
        val ry = lowData[7..5].int
        val r32 = lowData[4..0].int

        return construct(core, data, gpr16(ry), any(ProcType.SystemControlCop, Designation.General, r32, sel))
    }
}