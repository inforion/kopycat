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

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.ANY
import ru.inforion.lab403.kopycat.cores.base.operands.Displacement
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.cores.x86.enums.SSR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.interfaces.*
import java.math.BigInteger


class x86Displacement constructor(
    dtyp: Datatype,
    reg: x86Register,
    override val ssr: x86Register,
    off: Immediate<x86Core> = zero,
    access: Access = ANY
) : Displacement<x86Core>(dtyp, reg, off, access) {

    constructor(dtyp: Datatype, reg: x86Register, prefixes: Prefixes, off: Immediate<x86Core> = zero) :
        this(dtyp, reg, prefixes.ssr(reg), off)

    override fun value(core: x86Core): ULong = core.read(dtyp, effectiveAddress(core), ssr.reg)
    override fun value(core: x86Core, data: ULong): Unit = core.write(dtyp, effectiveAddress(core), data, ssr.reg)
    override fun extValue(core: x86Core): BigInteger = core.ine(effectiveAddress(core), dtyp.bytes, ssr.reg)
    override fun extValue(core: x86Core, data: BigInteger): Unit = core.oute(effectiveAddress(core), data, dtyp.bytes, ssr.reg)

    override fun toString(): String {
        val reg = reg as x86Register  // should be removed when all registers will be NG
        val mspec = dtyp.name.lowercase()
        val sspec = if (ssr.reg != SSR.DS.id) "$ssr:" else ""
        return when {
            reg.isNone -> "$mspec $sspec[$off]"
            else -> {
                val sign = if (off.signed && off.isNegative) "" else "+"
                when {
                    off.isZero -> "$mspec $sspec[$reg]"
                    else -> "$mspec $sspec[$reg$sign$off]"
                }
            }
        }
    }
}