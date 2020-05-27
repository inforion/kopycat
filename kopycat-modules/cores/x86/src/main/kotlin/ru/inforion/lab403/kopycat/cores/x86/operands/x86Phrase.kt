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
package ru.inforion.lab403.kopycat.cores.x86.operands

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.APhrase
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.cores.x86.enums.SSR
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.ebp
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.esp
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.bp
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.sp
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.ds
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.ss
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class x86Phrase(
        dtyp: Datatype,
        base: x86Register,
        index: x86Register,
        displ: Immediate<x86Core> = zero,
        val scale: Int = 1,
        ssr: x86Register = ds,
        access: AOperand.Access = AOperand.Access.ANY) :
        APhrase<x86Core>(dtyp, base, index, displ, access) {

    override val ssr: x86Register = if (base != bp && base != ebp && base != sp && base != esp) ssr else ss

    // index may be just 1, 2, 4 and 8 so we can place a cap on signext of index
    override fun effectiveAddress(core: x86Core): Long = base.value(core) + scale * index.value(core) + displ.ssext(core)

    override fun value(core: x86Core): Long = core.read(dtyp, effectiveAddress(core), ssr.reg)
    override fun value(core: x86Core, data: Long): Unit = core.write(dtyp, effectiveAddress(core), data, ssr.reg)

    override fun toString(): String {
        val ss = StringBuilder()
        ss.append(dtyp.name.toLowerCase())
        if (ssr.reg != SSR.DS.id)
            ss.append("$ssr:")
        ss.append("[")
        if (base.reg != x86GPR.NONE.id)
            ss.append("$base")
        if (index.reg != x86GPR.NONE.id) {
            ss.append("+")
            if (scale != 1)
                ss.append("$scale*")
            ss.append("$index")
        }
        if (displ.value != 0L)
            ss.append("$displ")
        ss.append("]")
        return ss.toString()
    }
}