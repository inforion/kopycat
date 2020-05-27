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
package ru.inforion.lab403.examples

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.extensions.unhexlify
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.AARMCPU
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.gdbstub.GDBServer
import ru.inforion.lab403.kopycat.modules.stm32f042.STM32F042
import ru.inforion.lab403.kopycat.modules.terminals.UartSerialTerminal

object stm32f042_bytes {
    @JvmStatic
    fun main(args: Array<String>) {
        // Create simple firmware
        // movs  r0, #3
        // movs  r1, #7
        // adds  r2, r1, r0
        val firmware = "0000000009000000032007210a18".unhexlify()

        // Create top-level module. It's necessarily only one top!
        val top = object : Module(null, "top") {
            // Place STM32F042 inside top module
            val mcu = STM32F042(this, "mcu", firmware)

            // Place virtual terminal -> will be created using socat
            // You could create virtual terminal by yourself using socat and specify path to /dev/tty...
            // For windows user you should use Com2Com and specify manually COMX from it
            val term1 = UartSerialTerminal(this, "term1", "socat:")

            init {
                // Make actual connection between STM32F042 and Virtual terminal
                buses.connect(mcu.ports.usart1_m, term1.ports.term_s)
                buses.connect(mcu.ports.usart1_s, term1.ports.term_m)

                // ARM debugger already in stm
            }
        }

        // initialize and reset top module and all inside
        top.initializeAndResetAsTopInstance()

        // start GDB server on port 23946
        val gdb = GDBServer(23946, true, binaryProtoEnabled = false)

        // connect GDB and device debugger
        gdb.debuggerModule(top.debugger)

        // HERE EMULATOR READ TO WORK WITH GDB
        // Below just code to see different API styles

        // step CPU core using debugger
        top.debugger.step()

        // read CPU register using debugger API
        var r0 = top.debugger.regRead(0)
        var r15 = top.debugger.regRead(15)
        println("using debugger API: r0 = 0x${r0.hex8} r15 = 0x${r15.hex8}")

        // read CPU register using core API
        r0 = top.core.reg(0)
        r15 = top.core.reg(15)
        println("using Core/CPU API: r0 = 0x${r0.hex8} r15 = 0x${r15.hex8}")

        // read CPU register using internal CPU API
        val arm = top.core.cpu as AARMCPU
        r0 = arm.regs.r0.value
        r15 = arm.regs.pc.value
        println("using internal API: r0 = 0x${r0.hex8} r15 = 0x${r15.hex8}")

        // process here will wait until debugger stop
    }
}