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
package ru.inforion.lab403.kopycat.cores.v850es.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.v850es.constructor
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esImmediate
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



class FormatXIIIP(core: v850ESCore, val construct: constructor) : ADecoder<AV850ESInstruction>(core) {

    override fun decode(s: Long): AV850ESInstruction {
        var size = 4
        val ff = s[20..19].toInt()
        val isLong = s[17]
        val imm = v850esImmediate(DWORD, s[5..1], false)
        val list = v850esImmediate(DWORD, s[0].insert(s[31..21], 11..1), false)

        if (isLong == 0L)
            return construct(core, size, arrayOf(imm, list))

        val epValue = when (ff) {
            0x00 -> {
                size = 4
                v850esRegister.GPR.r3
            }
            0x01 -> {
                size = 6
                v850esImmediate(DWORD, signext(s[47..32], 16).asLong, true)
            }
            0x02 -> {
                size = 6
                v850esImmediate(DWORD, s[47..32] shl 16, false)
            }
            0x03 -> {
                size = 8
                v850esImmediate(DWORD, s[63..32], false)
            }
            else -> throw GeneralException("Incorrect value")
        }
        return construct(core, size, arrayOf(imm, list, epValue))
    }
}