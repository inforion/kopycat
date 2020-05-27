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
package ru.inforion.lab403.kopycat.device

import ru.inforion.lab403.kopycat.cores.base.abstracts.ACPU

class TestCPU(core: TestCore, name: String): ACPU<TestCPU, TestCore, TestInstruction, TestGPR>(core, name) {
    override fun reg(index: Int): Long = regs[index].value(core as TestCore)
    override fun reg(index: Int, value: Long) = regs[index].value(core as TestCore, value)
    override fun count() = regs.count()

    override var pc: Long
        get() = regs.pc
        set(value) { regs.pc = value }

    val regs = TestGPRBank(core)

    override fun decode() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun execute(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}