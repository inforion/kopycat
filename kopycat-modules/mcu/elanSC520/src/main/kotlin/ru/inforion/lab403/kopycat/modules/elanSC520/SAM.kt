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
@file:Suppress("PropertyName", "MemberVisibilityCanBePrivate")

package ru.inforion.lab403.kopycat.modules.elanSC520

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINER
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.*
import ru.inforion.lab403.kopycat.cores.base.common.Debugger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts.ErrorAction.*
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.interfaces.IFetchReadWrite
import ru.inforion.lab403.kopycat.modules.BUS16
import ru.inforion.lab403.kopycat.modules.BUS32
import ru.inforion.lab403.kopycat.modules.elanSC520.SAM.TARGET_DEVICE.*
import ru.inforion.lab403.kopycat.modules.elanSC520.SAM.TARGET_DEVICE.SDRAM

/**
 *
 * System Address Mapping
 */
class SAM(parent: Module, name: String) : Module(parent, name) {
    companion object {
        const val MAPPING_HIGHEST_PRIO = 128
        const val MAPPING_LOWEST_PRIO = -2

        const val MMCR_UNMOVABLE_INDEX = -1
        const val MMCR_UNMOVABLE_BASE = 0xFFFE_F000uL

        const val MMCR_MOVABLE_INDEX = -2
        const val MMCR_WINDOW_SIZE = 0x1000uL
        const val MMCR_MOVABLE_PRIO = MAPPING_HIGHEST_PRIO

        const val BOOT_ROM_UNMOVABLE_INDEX = -3
        const val BOOT_ROM_UNMOVABLE_BASE = 0xFFFF_0000uL
        const val BOOT_ROM_UNMOVABLE_SIZE = 0x10000uL
        const val BOOT_ROM_UNMOVABLE_PRIO = MAPPING_LOWEST_PRIO

        const val SDRAM_DEFAULT_INDEX = -5
        const val SDRAM_DEFAULT_BASE = 0x0000_0000uL
        const val SDRAM_DEFAULT_SIZE = 0x1000_0000uL

        const val PCI_MEM_DEFAULT_SPACE_INDEX = -6
        const val PCI_MEM_DEFAULT_SPACE_BASE = 0x1000_0000uL
        const val PCI_MEM_DEFAULT_SPACE_SIZE = 0x3000_0000uL
        const val PCI_MEM_DEFAULT_SPACE_PRIO = MAPPING_LOWEST_PRIO

        const val PCI_MEM_DEDICATED_SPACE_INDEX = -7
        const val PCI_MEM_DEDICATED_SPACE_BASE = 0x4000_0000uL
        const val PCI_MEM_DEDICATED_SPACE_SIZE = 0xBFFE_F000uL
        const val PCI_MEM_DEDICATED_SPACE_PRIO = MAPPING_LOWEST_PRIO

        const val PCI_IO_DEFAULT_SPACE_INDEX = -8
        const val PCI_IO_DEFAULT_SPACE_BASE = 0x0400uL
        const val PCI_IO_DEFAULT_SPACE_SIZE = 0xFBFCuL  // -4 of CBAR
        const val PCI_IO_DEFAULT_SPACE_PRIO = MAPPING_LOWEST_PRIO

        const val SLAVE_GP_DMA_IO_HOLE_INDEX = -10
        const val MASTER_IC_IO_HOLE_INDEX = -11
        const val SLAVE2_IC_IO_HOLE_INDEX = -12
        const val PIT_IO_HOLE_INDEX = -13
        const val KEYBOARD_A20M_IO_HOLE_INDEX = -14
        const val SCPB_IO_HOLE_INDEX = -15
        const val RTC_IO_HOLE_INDEX = -16
        const val GP_DMA_IO_HOLE_INDEX = -17
        const val SCPA_IO_HOLE_INDEX = -18
        const val SLAVE1_IC_IO_HOLE_INDEX = -19
        const val MASTER_GP_DMA_IO_HOLE_INDEX = -20
        const val FPEIC_IO_HOLE_INDEX = -21
        const val UART1_IO_HOLE_INDEX = -22
        const val UART2_IO_HOLE_INDEX = -23

        const val PCI_IO_HOLE_INDEX = -24
        const val CBAR_IO_HOLE_INDEX = -25

        const val GPIO_BUS_IO_HOLE_INDEX = -26

        const val IO_HOLE_PRIORITY = MAPPING_HIGHEST_PRIO

        @Transient val log = logger(FINER)
    }

    inner class Ports : ModulePorts(this) {
        val x5mem = Port("x5mem")  // internal AMD memory bus
        val x5io = Port("x5io")  // internal AMD IO bus

        // SAM has config. regs in MMCR area
        // This area should be connected obviously to mmcro :)
        val mmcr_s = Port("mmcr_s")
        val gpio_s = Port("gpio_s")

        // Output buses from SAM after ADU (address decode unit)
        val gpcs = ports(8, "gpcs")
        val bootcs = Port("bootcs", onError = LOGGING)
        val romcs = ports(2, "romcs")
        val sdram = Port("sdram")

        val mmcr_m = Port("mmcr_m", onError = LOGGING)  // output port
        val gpio_m = Port("gpio_m", onError = EXCEPTION)  // output port

        val pci_mem = Port("pci_mem", onError = EXCEPTION)
        val pci_io = Port("pci_io", onError = LOGGING)
    }

    override val ports = Ports()

    enum class TARGET_DEVICE(val index: Int) {
        WINDOW_DISABLED(0b000),
        GP_BUS_IO(0b001),
        GP_BUS_MEMORY(0b010),
        PCI_BUS(0b011),
        BOOTCS(0b100),
        ROMCS1(0b101),
        ROMCS2(0b110),
        SDRAM(0b111)
    }

    @Suppress("PrivatePropertyName", "unused")
    private val PAR_TABLE = Array(16) { PARx(ports.mmcr_s, it) }

    private val extBus = arrayOf(
            ports.gpcs[0],   //  0
            ports.gpcs[1],   //  1
            ports.gpcs[2],   //  2
            ports.gpcs[3],   //  3
            ports.gpcs[4],   //  4
            ports.gpcs[5],   //  5
            ports.gpcs[6],   //  6
            ports.gpcs[7],   //  7
            ports.bootcs,    //  8
            ports.romcs[0],  //  9
            ports.romcs[1],  // 10
            ports.sdram      // 11
    )

    private fun disableAllMappingForIndex(index: Int) {
        memorySpace.disableMapping(index)
        ioSpace.disableMapping(index)
    }

    @Suppress("PrivatePropertyName", "unused")
    private val ADDDECCTL = object : Register(ports.mmcr_s, 0x80u, Datatype.BYTE, "ADDDECCTL") {
        val WPV_INT_ENB by bit(7)
        val IO_HOLE_DEST by bit(4)
        val RTC_DIS by bit(2)
        val UART2_DIS by bit(1)
        val UART1_DIS by bit(0)

        fun enableDisable(bit: Int, index: Int, range: ULongRange) {
            if (bit == 1)
                ioSpace.disableMapping(index)
            else if (!ioSpace.hasMapping(index))
                ioSpace.addIOHoleMapping(index, range)
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)

            check(WPV_INT_ENB == 0) { "Write-protect violations generate an interrupt request to the CPU -> Not implemented!" }
            check(IO_HOLE_DEST == 0) { "The accesses are forwarded to the PCI bus I/O space -> Not implemented!" }

            enableDisable(RTC_DIS, RTC_IO_HOLE_INDEX, 0x0070uL..0x0071uL)
            enableDisable(UART2_DIS, UART2_IO_HOLE_INDEX, 0x02F8uL..0x02FFuL)
            enableDisable(UART1_DIS, UART1_IO_HOLE_INDEX, 0x03F8uL..0x03FFuL)
        }
    }

    private inner class PARx(port: Port, val index: Int) :
            Register(port, 0x88uL + 0x04u * index.uint, DWORD, "PAR$index") {

        val TARGET by field(31..29)
        val ATTR by field(28..26)
        val PG_SZ by bit(25)
        val START_ADDR_17 by field(17..0)
        val START_ADDR_15 by field(15..0)
        val START_ADDR_13 by field(13..0)
        val REGION_SIZE_18 by field(24..18)
        val REGION_SIZE_16 by field(24..16)
        val REGION_SIZE_14 by field(24..14)

        val priority = 0x20 - index

        // +1 to region size is ok see page 3-14 of Élan™SC520 Microcontroller User’s Manual
        private val memPageSize get() = if (PG_SZ == 0) 0x1000u else 0x10000u
        private val memPageCount get() = if (PG_SZ == 0) REGION_SIZE_18 + 1uL else REGION_SIZE_14 + 1uL
        private val memRegionSize get() = memPageSize * memPageCount
        private val ioRegionSize get() = REGION_SIZE_16 + 1uL
        private val target get() = first<TARGET_DEVICE> { it.index.ulong_z == TARGET }
        private val memRegionStart get() = if (PG_SZ == 0)
            insert(START_ADDR_17, 29..12) else
            insert(START_ADDR_13, 29..16)
        private val ioRegionStart get() = START_ADDR_15

        private val nExec get() = ATTR[2].truth
        private val nCache get() = ATTR[1].truth
        private val nWrite get() = ATTR[0].truth

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)

            disableAllMappingForIndex(index)

            when (target) {
                WINDOW_DISABLED -> {
                    log.warning { "SAM mapping disabled ($index)" }
                    return
                }

                BOOTCS -> memorySpace.addBootROMMapping(index, memRegionStart, memRegionSize, priority, !nExec, !nWrite, !nCache)
                SDRAM -> memorySpace.addSDRAMMapping(index, memRegionStart, memRegionSize, priority, !nExec, !nWrite, !nCache)

                GP_BUS_MEMORY -> memorySpace.addGPBusMapping(index, ATTR.int, memRegionStart, memRegionSize, priority)
                GP_BUS_IO -> ioSpace.addGPBusMapping(index, ATTR.int, ioRegionStart, ioRegionSize, priority)

                ROMCS1 -> TODO("ROMCS1 not implemented")
                ROMCS2 -> TODO("ROMCS2 not implemented")

                PCI_BUS -> {
                    check(index == 0 || index == 1) { "SAM mapping PCI bus can be configure only in index 0 or 1" }
                    memorySpace.addPCIMemoryMapping(index, memRegionStart, memRegionSize, priority)
                }
            }
        }
    }

    @Suppress("PrivatePropertyName", "unused")
    private val CBAR = object : Register(ports.gpio_s, 0xFFFCu, DWORD, "CBAR") {
        val ENBL by bit(31)
        val ADR by field(29..12)
        val MATCH by field(7..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val cache = data
            super.write(ea, ss, size, value)
            if (MATCH != 0xCBuL) {
                // If control match not equal to CB return prev. state
                data = cache
            } else {
                log.fine { "CBAR written valid value = 0x${data.hex8}" }

                if (ENBL == 1) {
                    val baseOfMovableWindow = ADR shl 12

                    if (baseOfMovableWindow + MMCR_WINDOW_SIZE > MMCR_UNMOVABLE_BASE)
                        throw GeneralException("Rebaseable MMCR region overlap non-rebaseable!")

                    memorySpace.addMMCRMapping(MMCR_MOVABLE_INDEX, baseOfMovableWindow, MMCR_MOVABLE_PRIO)
                } else {
                    log.warning { "Disable moveable MMCR window!" }
                    memorySpace.disableMapping(MMCR_MOVABLE_INDEX)
                }
            }
        }
    }

    data class Region(
            val index: Int,
            val port: Port,
            val range: ULongRange,
            val priority: Int,
            val executable: Boolean,
            val writable: Boolean,
            val cacheable: Boolean,
            val dest: ULong): IFetchReadWrite {

        private val offset = dest - range.first

        constructor(
                index: Int,
                port: Port,
                base: ULong,
                size: ULong,
                priority: Int,
                executable: Boolean,
                writable: Boolean,
                cacheable: Boolean,
                dest: ULong = base
        ) : this(index, port, base until base + size, priority, executable, writable, cacheable, dest)

        override fun toString() = "" +
                "[${range.first.hex8}..${range.last.hex8}] priority=$priority " +
                "e=${executable.int} w=${writable.int} c=${cacheable.int} output=${port.toString()}"

        override fun fetch(ea: ULong, ss: Int, size: Int) = port.fetch(ea + offset, ss, size)
        override fun read(ea: ULong, ss: Int, size: Int) = port.read(ea + offset, ss, size)
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) = port.write(ea + offset, ss, size, value)
    }

    inner class SAM_SPACE(port: Port, size: ULong, name: String) : Area(port, 0u, size - 1u, name, ACCESS.R_W) {

        private val mapping = mutableListOf<Region>()

        private fun regionByAddress(ea: ULong) = mapping.filter { ea in it.range }.maxByOrNull { it.priority }

        private var accessedRegion: Region? = null

        private fun beforeFetchOrRead(from: Port, ea: ULong): Boolean {
            val region = regionByAddress(ea)

            if (region == null) {
                log.warning { "SAM on $this can't translate LOAD 0x${ea.hex8} from $from" }
                return false
            }

            // Debugger can't read MMCR registers
            if (from.module is Debugger && region.port != ports.bootcs && region.port != ports.sdram)
                return false

            accessedRegion = region
            return true
        }

        override fun beforeFetch(from: Port, ea: ULong, size: Int) = beforeFetchOrRead(from, ea)
        override fun beforeRead(from: Port, ea: ULong, size: Int) = beforeFetchOrRead(from, ea)

        override fun beforeWrite(from: Port, ea: ULong, size: Int, value: ULong): Boolean {
            val region = regionByAddress(ea)

            if (region == null) {
                log.warning { "SAM on $this can't translate STORE 0x${ea.hex8} from $from -> nothing found!" }
                return false
            }

            if (!region.writable) {
                log.warning { "SAM on $this can't translate STORE 0x${ea.hex8} from $from -> region $region not writable!" }
            }

            // Debugger can't write MMCR registers
            if (from.module is Debugger && region.port != ports.bootcs && region.port != ports.sdram)
                return false

            accessedRegion = region
            return true
        }

        override fun fetch(ea: ULong, ss: Int, size: Int) = accessedRegion!!.fetch(ea, ss, size)
        override fun read(ea: ULong, ss: Int, size: Int) = accessedRegion!!.read(ea, ss, size)
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) = accessedRegion!!.write(ea, ss, size, value)

        private fun addMapping(
                index: Int,
                port: Port,
                base: ULong,
                size: ULong,
                priority: Int,
                executable: Boolean,
                writable: Boolean,
                cacheable: Boolean,
                dest: ULong = base
        ): Region {
            disableMapping(index)
            val region = Region(index, port, base, size, priority, executable, writable, cacheable, dest)
            mapping.add(region)
            log.warning { "SAM mapping changed ($index) $region" }
            return region
        }

        fun disableMapping(index: Int) = mapping.removeIf { it.index == index }

        fun addMMCRMapping(index: Int, base: ULong, priority: Int = MAPPING_LOWEST_PRIO)
                = addMapping(index, ports.mmcr_m, base, MMCR_WINDOW_SIZE, priority, false, true, false, 0u)

        fun addBootROMMapping(index: Int, base: ULong, size: ULong, priority: Int, executable: Boolean, writable: Boolean, cacheable: Boolean)
                = addMapping(index, ports.bootcs, base, size, priority, executable, writable, cacheable)
        fun addSDRAMMapping(index: Int, base: ULong, size: ULong, priority: Int, executable: Boolean, writable: Boolean, cacheable: Boolean)
                = addMapping(index, ports.sdram, base, size, priority, executable, writable, cacheable)

        fun addGPBusMapping(index: Int, gpid: Int, base: ULong, size: ULong, priority: Int = MAPPING_LOWEST_PRIO)
                = addMapping(index, extBus[gpid], base, size, priority, false, true, false)

        fun addIOHoleMapping(index: Int, range: ULongRange, priority: Int = IO_HOLE_PRIORITY)
                = addMapping(index, ports.gpio_m, range.first, range.length, priority, false, true, false)
        fun addIOHoleMapping(index: Int, vararg address: ULong)
                = address.map { addIOHoleMapping(index, it..it) }

        fun addPCIMemoryMapping(index: Int, base: ULong, size: ULong, priority: Int)
                = addMapping(index, ports.pci_mem, base, size, priority, false, true, false)
        fun addPCIIOMapping(index: Int, base: ULong, size: ULong, priority: Int)
                = addMapping(index, ports.pci_io, base, size, priority, false, true, false)

        fun hasMapping(index: Int) = mapping.find { it.index == index } != null
    }

    private val memorySpace = SAM_SPACE(ports.x5mem, BUS32, "SAM_MEM_SPACE").apply {
        addBootROMMapping(BOOT_ROM_UNMOVABLE_INDEX, BOOT_ROM_UNMOVABLE_BASE, BOOT_ROM_UNMOVABLE_SIZE, BOOT_ROM_UNMOVABLE_PRIO, true, false, false)
        addMMCRMapping(MMCR_UNMOVABLE_INDEX, MMCR_UNMOVABLE_BASE)
        addSDRAMMapping(SDRAM_DEFAULT_INDEX, SDRAM_DEFAULT_BASE, SDRAM_DEFAULT_SIZE, MAPPING_LOWEST_PRIO, true, true, false)
        addPCIMemoryMapping(PCI_MEM_DEFAULT_SPACE_INDEX, PCI_MEM_DEFAULT_SPACE_BASE, PCI_MEM_DEFAULT_SPACE_SIZE, PCI_MEM_DEFAULT_SPACE_PRIO)
        addPCIMemoryMapping(PCI_MEM_DEDICATED_SPACE_INDEX, PCI_MEM_DEDICATED_SPACE_BASE, PCI_MEM_DEDICATED_SPACE_SIZE, PCI_MEM_DEDICATED_SPACE_PRIO)
    }

    private val ioSpace = SAM_SPACE(ports.x5io, BUS16, "SAM_IO_SPACE").apply {
        // see page 4-14 of Élan™SC520 Microcontroller User’s Manual
        addIOHoleMapping(SLAVE_GP_DMA_IO_HOLE_INDEX, 0x0000uL..0x000FuL)  // Slave GP-DMA Controller
        addIOHoleMapping(MASTER_IC_IO_HOLE_INDEX, 0x0020uL..0x0021uL)  // Master Interrupt Controller
        addIOHoleMapping(SLAVE2_IC_IO_HOLE_INDEX, 0x0024uL..0x0025uL)  // Slave 2 Interrupt Controller
        addIOHoleMapping(PIT_IO_HOLE_INDEX, 0x0040uL..0x0043uL)  // Programmable Interval Timer (PIT)
        addIOHoleMapping(KEYBOARD_A20M_IO_HOLE_INDEX, 0x60uL, 0x64uL)  // Keyboard Control A20M and Fast Reset (SCP)
        addIOHoleMapping(SCPB_IO_HOLE_INDEX, 0x61uL)  //LSystem Control Port B/NMI Status
        addIOHoleMapping(RTC_IO_HOLE_INDEX, 0x0070uL..0x0071uL)  // Real-Time Clock (RTC) Index/Data
        addIOHoleMapping(GP_DMA_IO_HOLE_INDEX, 0x80uL, 0x8FuL)  // General-Purpose Scratch Registers / GP-DMA Page Registers
        addIOHoleMapping(SCPA_IO_HOLE_INDEX, 0x92uL)  //LSystem Control Port A
        addIOHoleMapping(SLAVE1_IC_IO_HOLE_INDEX, 0x00A0uL..0x00A1uL)  // Slave 1 Interrupt Controller
        addIOHoleMapping(MASTER_GP_DMA_IO_HOLE_INDEX, 0x00C0uL..0x00DEuL)  // Master GP Bus DMA Controller
        addIOHoleMapping(FPEIC_IO_HOLE_INDEX, 0xF0uL)  //LFloating Point Error Interrupt Clear
        addIOHoleMapping(UART2_IO_HOLE_INDEX, 0x02F8uL..0x02FFuL)  // UART 2
        addIOHoleMapping(UART1_IO_HOLE_INDEX, 0x03F8uL..0x03FFuL)  // UART 1

        addIOHoleMapping(CBAR_IO_HOLE_INDEX, 0xFFFCuL..0xFFFFuL)  // PCI config registers

        // Default IO below 1Kb mapping to GP I/O bus
        addIOHoleMapping(GPIO_BUS_IO_HOLE_INDEX, 0x0000uL..0x03FFuL, MAPPING_LOWEST_PRIO)

        addPCIIOMapping(PCI_IO_HOLE_INDEX, 0xCF8uL, 8u, MAPPING_LOWEST_PRIO)  // PCI config registers

        // Default IO above 1Kb mapping to PCI I/O bus
        addPCIIOMapping(PCI_IO_DEFAULT_SPACE_INDEX, PCI_IO_DEFAULT_SPACE_BASE, PCI_IO_DEFAULT_SPACE_SIZE, PCI_IO_DEFAULT_SPACE_PRIO)
    }
}