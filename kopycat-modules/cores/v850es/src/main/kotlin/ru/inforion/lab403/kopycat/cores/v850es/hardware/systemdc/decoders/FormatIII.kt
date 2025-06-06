/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
package ru.inforion.lab403.kopycat.cores.v850es.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.common.extensions.signextRenameMeAfter
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.v850es.constructor
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esImmediate
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



class FormatIII(core: v850ESCore, val construct: constructor) : ADecoder<AV850ESInstruction>(core) {

    override fun decode(s: ULong): AV850ESInstruction {
        val cond = v850esImmediate(Datatype.BYTE, s[3..0], false)
        val value = s[6..4].insert(s[15..11], 7..3) shl 1
        val disp = v850esImmediate(Datatype.DWORD, value.signextRenameMeAfter(8), true)

        return construct(core, 2, arrayOf(disp, cond))
    }
}
