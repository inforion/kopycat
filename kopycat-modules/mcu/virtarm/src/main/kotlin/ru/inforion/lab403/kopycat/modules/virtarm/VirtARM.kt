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
package ru.inforion.lab403.kopycat.modules.virtarm

import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.common.extensions.toFile
import ru.inforion.lab403.common.proposal.emptyInputStream
import ru.inforion.lab403.kopycat.auxiliary.NANDGen
import ru.inforion.lab403.kopycat.auxiliary.NANDPart
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.modules.BUS32
import ru.inforion.lab403.kopycat.modules.NAND_BUS_SIZE
import ru.inforion.lab403.kopycat.modules.UART_MASTER_BUS_SIZE
import ru.inforion.lab403.kopycat.modules.UART_SLAVE_BUS_SIZE
import ru.inforion.lab403.kopycat.modules.cores.arm1176jzs.ARM1176JZS
import ru.inforion.lab403.kopycat.modules.cores.ARMDebugger
import ru.inforion.lab403.kopycat.modules.memory.RAM
import ru.inforion.lab403.kopycat.modules.terminals.UartSerialTerminal
import java.io.InputStream

class VirtARM(
        parent: Module?,
        name: String,
        tty: String = defaultTerminal,
        bootloaderContent: InputStream = Resource(defaultBootloaderPath).inputStream(),
        kernelContent: InputStream = Resource(defaultKernelPath).inputStream(),
        filesystemContent: InputStream = Resource(defaultFilesystemPath).inputStream(),
        kernelSymbols: InputStream? = Resource(defaultSymbolsPath).inputStream(),
        bootloaderCmd: String? = defaultBootloaderCmd
) : Module(parent, name) {

    companion object {
        const val defaultTerminal = "socat:"
        const val defaultBootloaderPath = "binaries/u-boot.bin"
        const val defaultKernelPath = "binaries/uImage"
        const val defaultSymbolsPath = "binaries/System.map"
        const val defaultFilesystemPath = "binaries/rootfs.ext2"
        const val defaultBootloaderCmd = "setenv machid 25f8\n" + // Set machine id for correct Linux boot
                "setenv bootargs console=ttyS0,115200n8 ignore_loglevel root=/dev/mtdblock0 init=/linuxrc lpj=622592\n" +
                "setenv verify n\n" +
                "bootm 1000000\n"

        // use static method because clean load most probably didn't require for Kopycat library manager loading
        fun clean(parent: Module?, name: String, tty: String) = VirtARM(
                parent,
                name,
                tty = tty,
                bootloaderContent = emptyInputStream,
                kernelContent = emptyInputStream,
                filesystemContent = emptyInputStream,
                kernelSymbols = null,
                bootloaderCmd = null)
    }

    constructor(
            parent: Module?,
            name: String,
            tty: String = defaultTerminal,
            bootloaderContentPath: String,
            kernelContentPath: String,
            filesystemContentPath: String,
            kernelSymbolsPath: String?,
            bootloaderCmd: String? = defaultBootloaderCmd
    ) : this(
            parent,
            name,
            tty,
            bootloaderContentPath.toFile().inputStream(),
            kernelContentPath.toFile().inputStream(),
            filesystemContentPath.toFile().inputStream(),
            kernelSymbolsPath?.toFile()?.inputStream(),
            bootloaderCmd)

    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem", BUS32)
        val irq = Bus("irq", BUS32)
        val rx_bus = Bus("rx_bus", UART_SLAVE_BUS_SIZE)
        val tx_bus = Bus("tx_bus", UART_MASTER_BUS_SIZE)
        val nand = Bus("nand", NAND_BUS_SIZE)
    }

    override val buses = Buses()

    private val arm1176jzs = ARM1176JZS(this, "arm1176jzs",  400.MHz, 0.25)
//    private val ram0 = RAM(this, "ram0", 0x24F00, Resource("binaries/u-boot.bin"))

    private val nandCtrl = NANDCtrl(this, "NANDCTRL")

    val nand = NANDGen.generate(
            this,
            "nand",
            NANDGen.Manufacturer.MICRON,
            NANDGen.NANDID.NAND_64MiB_1_8V_8_bit_set,
            32,
            2048,
            true)

    val nor = RAM(this, "NOR", 0x0004_0000, bootloaderContent)
    val kern = RAM(this, "KERNEL", 0x0030_0000, kernelContent)
    val sram = RAM(this, "SRAM", 0x0002_0000)
    val ddr0 = RAM(this, "DDR0", 0x2000_0000) // TODO: replace it

    //val ddr1 = RAM(this, "DDR1", 0x0400_0000)

    private val timer = Timer(this, "timer", 2)
    private val vic = PL190(this, "vic")

    private val dbg = ARMDebugger(this, "dbg")

    val uart = NS16550(this, "serial")
    val term = UartSerialTerminal(this, "term", tty)

    init {
        if (bootloaderCmd != null) {
            log.config { "Setting bootloaderCmd: '$bootloaderCmd'" }
            uart.sendText(bootloaderCmd)
        }

        if (kernelSymbols != null) {
            log.config { "Loading GCC map-file..." }
            arm1176jzs.info.loadGCCMapFile(kernelSymbols)
        }

        arm1176jzs.ports.mem.connect(buses.mem, 0x0000_0000L)
        nor.ports.mem.connect(buses.mem, 0x0000_0000L)
        kern.ports.mem.connect(buses.mem, 0x0100_0000L)
        sram.ports.mem.connect(buses.mem, 0x2000_0000L)
        ddr0.ports.mem.connect(buses.mem, 0x9000_0000L)
        //ddr1.ports.mem.connect(buses.mem, 0x0800_0000L)
        dbg.ports.breakpoint.connect(arm1176jzs.buses.virt)
        dbg.ports.reader.connect(arm1176jzs.buses.virt)

        timer.ports.mem.connect(buses.mem, 0x8021_0000L)
        timer.ports.irq.connect(buses.irq, 0)

        nandCtrl.ports.mem.connect(buses.mem, 0x8024_0000L)
        nandCtrl.ports.nand.connect(buses.nand)

        nand.ports.nand.connect(buses.nand)

        val dump = NANDPart(nand.pageSize, nand.pagesInBlock, nand.blockCount, nand.spareSize)
                .load(filesystemContent)
                .fillSpare(-1, -1)
                .fillECC()
                .buffer

        nand.load(dump)

        vic.ports.mem.connect(buses.mem, 0x8006_0000L)
        vic.ports.irq.connect(buses.irq)

        // Term to rx and tx
        term.ports.term_m.connect(buses.rx_bus)
        term.ports.term_s.connect(buses.tx_bus)

        uart.ports.mem.connect(buses.mem, 0x8023_0000L)
        uart.ports.tx.connect(buses.tx_bus)
        uart.ports.rx.connect(buses.rx_bus)
        uart.ports.irq.connect(buses.irq, 1)
    }
}