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
package ru.inforion.lab403.kopycat.modules.testbench

import ru.inforion.lab403.kopycat.interfaces.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.gdbstub.GDBServer

object Starter {
    @JvmStatic
    fun main(args: Array<String>) {
        // Create our Testbench device
        val top = Testbench(null, "testbench")

        // Initialize it as a top device (device that has no parent)
        top.initializeAndResetAsTopInstance()

        // Write some instructions into memory
        top.core.write(WORD, 0x0000_0000u, 0x2003u) // movs  r0, #3
        top.core.write(WORD, 0x0000_0002u, 0x2107u) // movs  r1, #7
        top.core.write(WORD, 0x0000_0004u, 0x180Au) // adds  r2, r1, r0

        // Setup program counter
        // Note, that we may use top.arm.cpu.pc but there is some caveat here
        // top.arm.cpu.pc just change PC but don't make flags changing (i.e. change core mode)
        // so be aware when change PC.
        top.arm.cpu.BXWritePC(0x0000_0000u)

        // Make a step
        top.arm.step()
        assert(top.core.reg(0) == 3uL)

        // Make another step
        top.arm.step()
        assert(top.core.reg(1) == 7uL)

        // And one more step
        top.arm.step()
        assert(top.core.reg(2) == 10uL)

        GDBServer(23946).also { it.debuggerModule(top.debugger) }
    }
}