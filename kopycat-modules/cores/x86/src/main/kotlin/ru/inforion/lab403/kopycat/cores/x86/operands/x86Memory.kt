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

import ru.inforion.lab403.common.extensions.long
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.ANY
import ru.inforion.lab403.kopycat.cores.base.operands.Memory
import ru.inforion.lab403.kopycat.cores.x86.enums.SSR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.interfaces.*
import java.math.BigInteger

class x86Memory constructor(dtyp: Datatype, atyp: Datatype, addr: ULong, override val ssr: x86Register, access: Access = ANY) :
        Memory<x86Core>(dtyp, atyp, addr, access) {

    constructor(dtyp: Datatype, addr: ULong, prefixes: Prefixes) :
            this(dtyp, prefixes.addrsize, addr, prefixes.ssr())

    override fun effectiveAddress(core: x86Core) = addr like atyp

    override fun value(core: x86Core): ULong = core.read(dtyp, effectiveAddress(core), ssr.reg).also {
        require(dtyp.bytes <= 8) { "Can't read ${dtyp.bytes}" }
    }
    override fun value(core: x86Core, data: ULong): Unit = core.write(dtyp, effectiveAddress(core), data, ssr.reg).also {
        require(dtyp.bytes <= 8) { "Can't read ${dtyp.bytes}" }
    }

    override fun extValue(core: x86Core): BigInteger = core.ine(effectiveAddress(core), dtyp.bytes, ssr.reg)
    override fun extValue(core: x86Core, data: BigInteger): Unit = core.oute(effectiveAddress(core), data, dtyp.bytes, ssr.reg)

    override fun toString() = buildString {
        if (ssr.reg != SSR.DS.id)
            append("$ssr:")
        append("${dtyp}_%08X".format(addr.long))
    }
}