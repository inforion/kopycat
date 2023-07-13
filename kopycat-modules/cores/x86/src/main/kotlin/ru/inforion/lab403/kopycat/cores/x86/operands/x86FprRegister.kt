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

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import java.math.BigInteger

class x86FprRegister(reg: Int) : ARegister<x86Core>(reg, Access.ANY, dtyp = Datatype.FPU80) {
    @Deprecated("Use extValue instead", ReplaceWith("extValue"))
    override fun value(core: x86Core): ULong = throw RuntimeException("Use extValue instead")

    @Deprecated("Use extValue instead", ReplaceWith("extValue"))
    override fun value(core: x86Core, data: ULong) = throw RuntimeException("Use extValue instead")

    override fun extValue(core: x86Core) = core.fpu.st(reg)

    override fun extValue(core: x86Core, data: BigInteger) = core.fpu.st(reg, data)

    fun push(core: x86Core, data: BigInteger) = core.fpu.push(data)

    override fun toString() = "fpr[${reg}]"
}
