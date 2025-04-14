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
package ru.inforion.lab403.kopycat.cores.x86.operands

import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.long
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.Near
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class x86Near private constructor(
    dtyp: Datatype,
    val x86offset: ULong,
    override val ssr: x86Register,
    private val insnLen: Int,
    private val opsize: Datatype,
) : Near<x86Core>(x86offset.int, dtyp) {

    constructor(dtyp: Datatype, offset: ULong, prefixes: Prefixes, insnLen: Int) :
            this(dtyp, offset, prefixes.ssr(), insnLen, prefixes.opsize)

    override fun value(core: x86Core) = x86offset
    override fun toString(): String {
        var actualOffset = x86offset.long + insnLen
        if (dtyp != opsize) {
            actualOffset = actualOffset signext dtyp.msb
        }

        return "0x%04X".format(actualOffset like opsize)
    }
}
