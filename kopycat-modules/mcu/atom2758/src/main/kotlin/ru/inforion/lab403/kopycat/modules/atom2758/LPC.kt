/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.kopycat.modules.atom2758

import ru.inforion.lab403.common.extensions.bzero
import ru.inforion.lab403.common.extensions.clr
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.base.extensions.mapOffset
import ru.inforion.lab403.kopycat.modules.common.pci.PciAbstract
import ru.inforion.lab403.kopycat.modules.common.pci.PciDevice
import java.util.logging.Level
import java.util.logging.Level.CONFIG

class LPC(parent: Module, name: String) : PciDevice(parent, name, 0x8086, 0x1F38) {
    inner class SPEC_BAR(reg: Int, name: String, var range: Int, var area: Int, val bus: Int) :
        PCI_CONF_FUNC_WR(reg, DWORD, name, 0u, Level.WARNING) {

        val PREF by bit(3)
        val ADDRNG by bit(2)
        val EN by bit(1)
        val MEMI by bit(0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value clr 0)
            require(ADDRNG == 0) { "64 bit BAR no implemented" }
            // should be used field BA from datasheet but just zero last 4 bits may be enough
            if (EN == 1) ports.mapper.mapOffset(name, value bzero 3..0, range, area, bus)
        }
    }

    inner class RCRB_BAR(reg: Int, name: String, var range: Int, var area: Int, val bus: Int) :
        PCI_CONF_FUNC_WR(reg, DWORD, name, 0u, Level.WARNING) {

        val EN by bit(0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value bzero 9..1)
            // EN not check because firmware not write to EN bit but wants this area be mapped
            ports.mapper.mapOffset(name, value bzero 9..0, range, area, bus)
        }
    }

    val BAR_10 = PCI_BAR(0x10, DWORD, "BAR_10")
    val BAR_14 = PCI_BAR(0x14, DWORD, "BAR_14")
    val BAR_18 = PCI_BAR(0x18, DWORD, "BAR_18")
    val BAR_1C = PCI_BAR(0x1C, DWORD, "BAR_1C")
    val BAR_20 = PCI_BAR(0x20, DWORD, "BAR_20")
    val BAR_24 = PCI_BAR(0x24, DWORD, "BAR_24")

    val ACPI_BASE_ADDRESS = SPEC_BAR(0x40,"ACPI_BASE_ADDRESS", ACPI.BUS_SIZE, BRIDGE.IO_AREA, ACPI.BUS_INDEX)
    val PMC_BASE_ADDRESS = SPEC_BAR(0x44, "PMC_BASE_ADDRESS", PMC.BUS_SIZE, BRIDGE.MEMORY_AREA, PMC.BUS_INDEX)
    val GPIO_BASE_ADDRESS = SPEC_BAR(0x48, "GPIO_BASE_ADDRESS", GPIO.BUS_SIZE, BRIDGE.IO_AREA, GPIO.BUS_INDEX)
    val IO_CONTROLLER_BASE_ADDRESS = SPEC_BAR(0x4C, "IO_CONTROLLER_BASE_ADDRESS",  IOC.BUS_SIZE, BRIDGE.MEMORY_AREA, IOC.BUS_INDEX)
    val ILB_BASE_ADDRESS = SPEC_BAR(0x50, "ILB_BASE_ADDRESS",  ILB.BUS_SIZE, BRIDGE.MEMORY_AREA, ILB.BUS_INDEX)
    val SPI_BASE_ADDRESS = SPEC_BAR(0x54, "SPI_BASE_ADDRESS",  SPI.BUS_SIZE, BRIDGE.MEMORY_AREA, SPI.BUS_INDEX)
    val MPHY_BASE_ADDRESS = SPEC_BAR(0x58, "MPHY_BASE_ADDRESS",  MPHY.BUS_SIZE, BRIDGE.MEMORY_AREA, MPHY.BUS_INDEX)
    val PUNIT_BASE_ADDRESS = SPEC_BAR(0x5C, "PUNIT_BASE_ADDRESS", PUNIT.BUS_SIZE, BRIDGE.MEMORY_AREA, PUNIT.BUS_INDEX)

    val PCIE_REG_BIOS_DECODE_EN = PCI_CONF_FUNC_WR(0xD8, WORD, "PCIE_REG_BIOS_DECODE_EN", 0xFFCFu, level = CONFIG)

    val UART_CONT = PCI_CONF_FUNC_WR(0x80, DWORD, "UART_CONT", level = CONFIG)

    val RCRB_BASE_ADDRESS = RCRB_BAR(0xF0, "RCRB_BASE_ADDRESS", RCRB.BUS_SIZE, BRIDGE.MEMORY_AREA, RCRB.BUS_INDEX)

    val REG_A0 = PCI_CONF_FUNC_WR(0xA0, DWORD, "REG_A0", 0u, level = CONFIG)
    val REG_A4 = PCI_CONF_FUNC_WR(0xA4, DWORD, "REG_A4", 0u, level = CONFIG)
    val REG_EA = PCI_CONF_FUNC_WR(0xEA, DWORD, "REG_EA", 0u, level = CONFIG)
}