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
package ru.inforion.lab403.kopycat.cores.arm.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class SPRBank : ARegisterBankNG(32) {
    override val name = "ARM Special Purpose Registers Bank"

    class Operand(reg: Int, access: Access = Access.ANY) : ARegister<AARMCore>(reg, access) {
        override fun toString(): String = "SPR[$reg]" // TODO: replace it
        override fun value(core: AARMCore, data: Long) = core.cpu.spr.write(reg, data)
        override fun value(core: AARMCore): Long = core.cpu.spr.read(reg)
    }

    inner class PRIMASK : Register() {
        var pm by bitOf(0)
    }

    inner class CONTROL : Register() {
        var npriv by bitOf(0)
        var spsel by bitOf(1)
    }

    val primask = PRIMASK()
    val control = CONTROL()

    init {
        initialize()
    }
}