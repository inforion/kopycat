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
package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.common.Debugger
import ru.inforion.lab403.kopycat.cores.base.common.Module


class v850ESDebugger(parent: Module, name: String): Debugger(parent, name) {
    override fun ident() = "v850es"

    override fun registers(): MutableList<Long> {
        val core = core as v850ESCore
        val gprRegs = Array(core.cpu.regs.count()) { k -> regRead(k) }
        val ctrlRegs = Array(core.cpu.cregs.count()) { k -> readCtrlRegister(core, k) }
        val flags = Array(core.cpu.flags.count()) { k -> readFlags(core, k) }
        val result = gprRegs.toMutableList()
        result.addAll(ctrlRegs)
        result.addAll(flags)

        return result
    }

    // TODO(): Fix readFlags and readCtrlRegister
    private fun readFlags(core: v850ESCore, index: Int): Long = core.cpu.cregs.psw[index]

    private fun readCtrlRegister(core: v850ESCore, index: Int): Long = core.cpu.cregs.readIntern(index)
}