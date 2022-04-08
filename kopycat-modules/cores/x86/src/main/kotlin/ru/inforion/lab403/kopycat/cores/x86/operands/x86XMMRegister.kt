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

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import java.math.BigInteger

class x86XMMRegister(reg: Int) : ARegister<x86Core>(reg, Access.ANY) {

    override fun value(core: x86Core): ULong = core.sse.xmm[reg].int.ulong_z

    override fun value(core: x86Core, data: ULong) {
        core.sse.xmm[reg] = BigInteger(data.hex4, 16)
    }

    override fun bytes(core: x86Core, size: Int): ByteArray {
        require(size == 16) { "Wrong requested size of xmm register" }
        val value = core.sse.xmm[reg]
        return value.toByteArray().reversedArray().copyOf(size)
    }

    override fun bytes(core: x86Core, data: ByteArray) {
        require(data.size == 16) { "Wrong size of of array to store xmm register" }
        core.sse.xmm[reg] = BigInteger(data.reversedArray()) // cus big-endian
    }

    override fun toString() = "xmm${reg}"
}