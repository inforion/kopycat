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
package ru.inforion.lab403.kopycat.modules.common

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.WARNING
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.Port
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.extensions.mapOffset
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.modules.*
import ru.inforion.lab403.kopycat.modules.common.pci.PciDevice

@Suppress("unused", "PropertyName", "ClassName")
class i82551(parent: Module, name: String) : PciDevice(
    parent,
    name,
    0x8086,
    0x1209,
    0x10,  // 82551
    0x200000,  // ethernet controller
    0,  // disabled
    0x70,
    0x8086,
    0x0000_0000,
    0,
    1,  // INTA# used
    0xFF,
    0xFF
) {
    companion object {
        @Transient private val log = logger(FINE)
        
        const val MEM_BUS_INDEX = 0
        const val IO_BUS_INDEX = 1
        const val FLASH_BUS_INDEX = 2
    }

    val mapper = ports.Port("mapper")

    val mem = ports.Port("mem")
    val io = ports.Port("io")

    val spi = ports.Port("spi")

    inner class EEPROM_CONTROL_REG(port: Port, address: ULong, name: String) : Register(port, address, WORD, name) {
        override fun read(ea: ULong, ss: Int, size: Int): ULong = spi.read(ea - address, ss, size)
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) = spi.write(ea - address, ss, size, value)
    }

    inner class BAR constructor(
        reg: Int,
        name: String,

        base: ULong = 0u,
        val range: Int = 0,  // size, because name 'size' clash with read/write size
        area: Int = PCI_MEM_AREA,

        val bus: Int = -1,
    ) : PCI_CONF_FUNC_WR(reg, DWORD, name, base or area.ulong_z, WARNING) {

        private inline val base get() = data and 0xFFFF_FFFEu

        private inline val area get() = data[0].int

        private inline val sizeAndArea get() = (-range or area).ulong_z

        private inline val baseAndArea get() = data

        private var sizeRequested = false

        override fun read(ea: ULong, ss: Int, size: Int) =
            if (sizeRequested) sizeAndArea.also { sizeRequested = false } else baseAndArea

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            if (data == PCI_REQUEST_SPACE_SIZE) {
                sizeRequested = true
                return
            }
            mapper.mapOffset(name, base, range, area, bus)
        }

        override fun reset() {
            super.reset()
            mapper.mapOffset(name, base, range, area, bus)
        }
    }

    val CSRMAP_BAR = BAR(0x10, "CSRMAP_BAR", 0xD000_0000u, 0x1000, PCI_MEM_AREA, MEM_BUS_INDEX)
    val CSRIO_BAR = BAR(0x14, "CSRIO_BAR", 0x0000_D000u, 0x20, PCI_IO_AREA, IO_BUS_INDEX)
    val FLASH_BAR = BAR(0x18, "FLASH_BAR", 0xD010_0000u, 0x100000, PCI_MEM_AREA, FLASH_BUS_INDEX)

    val MEM_CSR_SCB_SW = Register(mem, 0x00u, WORD, "MEM_CSR_SCB_SW")

    val MEM_CSR_SCB_CW = object : ByteAccessRegister(mem, 0x02u, WORD, "MEM_CSR_SCB_CW") {
        var CX by bit(15)
        var FR by bit(14)
        var CNA by bit(13)
        var RNR by bit(12)
        var MDI by bit(11)
        var SWI by bit(10)
        var FCP by bit(8)
        var CUS by field(7..6)
        var RUS by field(5..2)
        var rsvd by field(1..0)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            CUS = 0u
            RUS = 0u
            rsvd = 0u
            return super.read(ea, ss, size)
        }
    }

    val MEM_CSR_SCB_GP = Register(mem, 0x04u, DWORD, "MEM_CSR_SCB_GP")
    val MEM_CSR_PORT = Register(mem, 0x08u, DWORD, "MEM_CSR_PORT")

    /**
     * See page 50
     *
    The 8255x MII management interface allows the CPU control over the PHY unit
    via a control register in the 8255X.  This register, called the Management
    Data Interface (MDI) Control Register, allows driver software to place the
    PHY in specific modes and query the PHY unit for link status.  The structure
    of the MDI Control Register is described in the following figure.

    +-----+--+--+-----+----------+----------+----------------------+
    |31 30|29|28|27 26|25      21|20      16|15                   0|
    +-----+--+--+-----+----------+----------+----------------------+
    | 0  0| I| R|  OP |  PHYADD  |  REGADD  |         DATA         |
    +-----+--+--+-----+----------+----------+----------------------+

    Where:

    Bits     Name
    -----------------------------
    0-15    Data
    16-20    PHY Register Address
    21-25    PHY Address
    26-27    Opcode
    28       Ready
    29       Interrupt Enable
    30-31    Reserved
     */
    @Suppress("MemberVisibilityCanBePrivate")
    inner class CSR_MDI_CR(port: Port, address: ULong, name: String) : Register(port, address, DWORD, name) {
        var IE by bit(29)  // 29
        var R by bit(28)  // 28
        var Opcode by field(27..26)  // 27..26
        var PHYAdd by field(25..21)  // 25..21
        var RegAdd by field(20..16)  // 20..16
        var Data_ by field(15..0)

        override fun stringify() = "${super.stringify()} [IE=$IE R=$R Opcode=$Opcode PHYAdd=$PHYAdd RegAdd=$RegAdd data=$Data_]"

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            R = 1
            return super.read(ea, ss, size)
        }
    }

    val MEM_CSR_MDI_CR = CSR_MDI_CR(mem, 0x10u, "MEM_CSR_MDI_CR")

    val MEM_CSR_EEPROM_CR = EEPROM_CONTROL_REG(mem, 0x0Eu, "MEM_CSR_EEPROM_CR")
    val MEM_CSR_RX_DMA_BC = Register(mem, 0x14u, DWORD, "MEM_CSR_RX_DMA_BC")
    val MEM_CSR_FCR = Register(mem, 0x19u, WORD, "MEM_CSR_FCR")
    val MEM_CSR_PMDR = Register(mem, 0x1Bu, BYTE, "MEM_CSR_PMDR")
    val MEM_CSR_GC = Register(mem, 0x1Cu, BYTE, "MEM_CSR_GC")
    val MEM_CSR_GS = Register(mem, 0x1Du, BYTE, "MEM_CSR_GS")
    val MEM_CSR_FER = Register(mem, 0x30u, DWORD, "MEM_CSR_FER")
    val MEM_CSR_FEMR = Register(mem, 0x34u, DWORD, "MEM_CSR_FEMR")
    val MEM_CSR_FPSR = Register(mem, 0x38u, DWORD, "MEM_CSR_FPSR")
    val MEM_CSR_FCRER = Register(mem, 0x3Cu, DWORD, "MEM_CSR_FCRER")

    val IO_CSR_SCB_SW = Register(io, 0x00u, WORD, "IO_CSR_SCB_SW")
    val IO_CSR_SCB_CW = Register(io, 0x02u, WORD, "IO_CSR_SCB_CW")
    val IO_CSR_SCB_GP = Register(io, 0x04u, DWORD, "IO_CSR_SCB_GP")
    val IO_CSR_PORT = Register(io, 0x08u, DWORD, "IO_CSR_PORT")
    val IO_CSR_EEPROM_CR = EEPROM_CONTROL_REG(io, 0x0Eu, "IO_CSR_EEPROM_CR")
    val IO_CSR_MDI_CR = CSR_MDI_CR(io, 0x10u, "IO_CSR_MDI_CR")
    val IO_CSR_RX_DMA_BC = Register(io, 0x14u, DWORD, "IO_CSR_RX_DMA_BC")
    val IO_CSR_FCR = Register(io, 0x19u, WORD, "IO_CSR_FCR")
    val IO_CSR_PMDR = Register(io, 0x1Bu, BYTE, "IO_CSR_PMDR")
    val IO_CSR_GC = Register(io, 0x1Cu, BYTE, "IO_CSR_GC")
    val IO_CSR_GS = Register(io, 0x1Du, BYTE, "IO_CSR_GS")
    val IO_CSR_FER = Register(io, 0x30u, DWORD, "IO_CSR_FER")
    val IO_CSR_FEMR = Register(io, 0x34u, DWORD, "IO_CSR_FEMR")
    val IO_CSR_FPSR = Register(io, 0x38u, DWORD, "IO_CSR_FPSR")
    val IO_CSR_FCRER = Register(io, 0x3Cu, DWORD, "IO_CSR_FCRER")


    // additional PCI function 0 registers
    val CAP_IDENT = PCI_CONF_FUNC_RD(0xDC, BYTE, "CAP_IDENT")
    val NEXT_ITEM_PTR = PCI_CONF_FUNC_RD(0xDD, BYTE, "NEXT_ITEM_PTR")
    val POWER_CAPABILITIES = PCI_CONF_FUNC_RD(0xDE, WORD, "POWER_CAPABILITIES")

    val POWER_MANAGEMENT_CSR = PCI_CONF_FUNC_RD(0xE0, WORD, "POWER_MANAGEMENT_CSR")
    val DATA = PCI_CONF_FUNC_WR(0xE2, BYTE, "DATA")
}