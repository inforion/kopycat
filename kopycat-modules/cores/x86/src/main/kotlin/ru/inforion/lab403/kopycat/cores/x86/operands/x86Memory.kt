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
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.ANY
import ru.inforion.lab403.kopycat.cores.base.operands.Memory
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.ds
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class x86Memory(dtyp: Datatype, addr: Long, override val ssr: x86Register = ds, access: Access = ANY) :
        Memory<x86Core>(dtyp, DWORD, addr, access) {
    override fun value(core: x86Core): Long = core.read(dtyp, effectiveAddress(core), ssr.reg)
    override fun value(core: x86Core, data: Long): Unit = core.write(dtyp, effectiveAddress(core), data, ssr.reg)
}