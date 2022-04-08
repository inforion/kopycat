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

import ru.inforion.lab403.common.extensions.truth
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.instructions.decoders.ADecoder
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * SAVE, RESTORE
 */

class I8_SVRS(core: MipsCore,
              val constructLow: (MipsCore, ULong, Boolean, Boolean, Boolean, Boolean, Int) -> AMipsInstruction,
              val constructExt: (MipsCore, ULong, Int, Int, Int, Boolean, Boolean, Boolean, Boolean) -> AMipsInstruction) : ADecoder(core) {
    override fun decode(data: ULong): AMipsInstruction {
        val lowData = getLowData(data)

        val s = lowData[7].truth
        val ra = lowData[6].truth
        val s0 = lowData[5].truth
        val s1 = lowData[4].truth
        val framesize = lowData[3..0].int

        if (!isExtended(data))
            return constructLow(core, data, s, ra, s0, s1, framesize)

        val xregs = data[10..8].int
        val framesizeExt = (data[7..4].int shl 4) or framesize
        val aregs = data[3..0].int

        return constructExt(core, data, xregs, framesizeExt, aregs, s, ra, s0, s1)
    }
}