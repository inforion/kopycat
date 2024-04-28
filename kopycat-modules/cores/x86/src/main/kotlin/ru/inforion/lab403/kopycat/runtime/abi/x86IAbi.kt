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
package ru.inforion.lab403.kopycat.runtime.abi

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.x86.enums.SSR
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.interfaces.IValuable
import ru.inforion.lab403.kopycat.modules.cores.x86Core


interface x86IAbi : IAbi {
    class RegisterValuable(
        val core: x86Core,
        private val gprReg: x86GPR
    ) : IValuable {
        private val reg get() = core.cpu.regs.gpr(gprReg, x86utils.modeToDatatype(core))

        override var data: ULong
            get() = reg.value
            set(value) {
                reg.value = value
            }
    }

    override val core: x86Core

    override fun getStackAlignment(): Datatype = x86utils.modeToDatatype(core)

    fun getSPReg() = core.cpu.regs.gpr(x86GPR.RSP, x86utils.modeToDatatype(core))

    override var sp: ULong
        get() = getSPReg().value
        set(value) {
            getSPReg().value = value
        }


    override var pc: ULong
        get() = core.pc
        set(value) {
            core.pc = value
        }

    override val ss: Int
        get() = SSR.SS.id


    override fun growStackAddress(addr: ULong, alignedSize: ULong): ULong = addr - alignedSize

    override fun shrinkStackAddress(addr: ULong, alignedSize: ULong): ULong = addr + alignedSize

    override fun saveState(): List<ULong> = (0..16)
        .map { i ->
            core.cpu.regs.gpr(x86GPR.byIndex(i), Datatype.QWORD).value
        }
        .toList()

    override fun restoreState(state: List<ULong>) = state
        .forEachIndexed { i, it ->
            core.cpu.regs.gpr(x86GPR.byIndex(i), Datatype.QWORD).value = it
        }

    override fun call(startAddress: ULong) {
        // TODO: documentation. We will return to pc to the current instruction, cause we didn't execute it
        pushStack(pc)
        pc = startAddress
    }
}