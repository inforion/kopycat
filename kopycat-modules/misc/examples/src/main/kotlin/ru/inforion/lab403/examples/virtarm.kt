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
import ru.inforion.lab403.kopycat.auxiliary.NANDPart
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.AARMCPU
import ru.inforion.lab403.kopycat.gdbstub.GDBServer
import ru.inforion.lab403.kopycat.modules.virtarm.VirtARM
import java.io.ByteArrayInputStream

object virtarm {
    @JvmStatic
    fun main(args: Array<String>) {
        // Create clean virtual ARM device on ARM1176JZS core
        val top = VirtARM.clean(null, "top", "socat:").also { it.initializeAndResetAsTopInstance() }

        // Bootloader and Kernel is RAM and has memory interface:
        // - store to write block into memory
        // - load to read block from memory
        // - write to write at most 8 bytes with taking into account endian
        // - read to read at most 8 bytes with taking into account endian

        // here first instruction that will be executed
        top.nor.store(0, "0300b0e30710b0e3002091e0".unhexlify())

        // here is some RAM
        // in default VirtARM configuration kernel placed here and UBOOT go in this region
        top.kern.store(0, "AAAAAAAA".unhexlify())

        // NAND isn't normal memory and has no standard memory interface so we need
        // first - make right memory layout
        // second - load into memory this data
        with(top.nand) {
            // Create some data
            val stream = ByteArrayInputStream("ABACAD00".unhexlify())

            val dump = NANDPart(pageSize, pagesInBlock, blockCount, spareSize)
                    .load(stream)
                    .fillSpare(-1, -1)
                    .fillECC()
                    .buffer

            load(dump)
        }

        // send some text into uart
        top.uart.sendText("")

        // start GDB server on port 23946
        GDBServer(23946, true, binaryProtoEnabled = false).also { it.debuggerModule(top.debugger) }

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