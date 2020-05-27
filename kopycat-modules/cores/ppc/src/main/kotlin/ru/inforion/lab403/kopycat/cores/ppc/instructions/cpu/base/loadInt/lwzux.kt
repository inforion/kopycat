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
package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.loadInt

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Load word and zero with update indexed
class lwzux(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "lwzux"

    val rt = PPCRegister.gpr(fieldA)
    val ra = PPCRegister.gpr(fieldB)
    val rb = PPCRegister.gpr(fieldC)

    override fun execute() {
        if (((ra as PPCRegister).reg == 0) || ((ra as PPCRegister).reg == (rt as PPCRegister).reg))
            throw GeneralException("Forbidden combination")

        val ea = ra.value(core) + rb.value(core)
        val mem = core.inl(ea)
        rt.value(core, mem)
        ra.value(core, ea)
    }
}