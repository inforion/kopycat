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
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.instructions.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by shiftdj on 21.06.2021.
 */

class RI(core: MipsCore,
         val constructLow: (MipsCore, ULong, MipsRegister, MipsImmediate) -> AMipsInstruction = { _, _, _, _ -> throw NotImplementedError("Not implemented") },
         val constructHigh: (MipsCore, ULong, MipsRegister, MipsImmediate) -> AMipsInstruction = { _, _, _, _ -> throw NotImplementedError("Not implemented") },
        ) : ADecoder(core) {

    override fun decode(data: ULong): AMipsInstruction {
        val lowData = getLowData(data)

        val rx = lowData[10..8].int
        val immediateLow = lowData[7..0]

        if (!isExtended(data))
            return constructLow(core, data, gpr16(rx), imm(immediateLow))

        val immediateFull = (data[4..0] shl 11) or (data[10..5] shl 5) or immediateLow[4..0]

        return constructHigh(core, data, gpr16(rx), imm(immediateFull))
    }
}