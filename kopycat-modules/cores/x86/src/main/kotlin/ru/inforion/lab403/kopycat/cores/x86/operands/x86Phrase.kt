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
package ru.inforion.lab403.kopycat.cores.x86.operands

import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.APhrase
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.cores.x86.enums.SSR
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.interfaces.*


class x86Phrase private constructor(
    dtyp: Datatype,
    val atyp: Datatype,
    base: x86Register,
    index: x86Register,
    override val ssr: x86Register,
    displ: Immediate<x86Core> = zero,
    val scale: Int = 1,
    access: Access = Access.ANY
) : APhrase<x86Core>(dtyp, base, index, displ, access) {

    constructor(
        dtyp: Datatype,
        base: x86Register,
        index: x86Register,
        prefixes: Prefixes,
        displ: Immediate<x86Core> = zero,
        scale: Int = 1
    ) : this(dtyp, prefixes.addrsize, base, index, prefixes.ssr(base), displ, scale)

    private fun calculateAddress(core: x86Core): ULong =
        base.value(core) + scale.uint * index.value(core) + displ.usext(core)

    // index may be just 1, 2, 4 and 8 so we can place a cap on signext of index
    override fun effectiveAddress(core: x86Core): ULong = calculateAddress(core) like atyp

    override fun value(core: x86Core): ULong = core.read(dtyp, effectiveAddress(core), ssr.reg)
    override fun value(core: x86Core, data: ULong): Unit = core.write(dtyp, effectiveAddress(core), data, ssr.reg)

    override fun toString() = buildString {
        // should be removed when all registers will be NG
        val base = base as x86Register
        val index = index as x86Register

        append(dtyp.name.lowercase())

        if (ssr.reg != SSR.DS.id)
            append(" $ssr:")
        append("[")

        if (base.isNotNone)
            append("$base")
        if (index.isNotNone) {
            if (base.isNotNone)
                append("+")
            if (scale != 1)
                append("$scale*")
            append("$index")
        }
        if (displ.value != 0uL) {
            append("+$displ")
        }
        append("]")
    }
}