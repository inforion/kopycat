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
package ru.inforion.lab403.kopycat.modules.common

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.SlavePort
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.modules.PCI_CSR0
import ru.inforion.lab403.kopycat.modules.PCI_CSR1
import ru.inforion.lab403.kopycat.modules.PIN
import ru.inforion.lab403.kopycat.modules.common.pci.PCITarget
import java.util.logging.Level

@Suppress("unused", "PropertyName", "ClassName")

class i82551(parent: Module, name: String) : PCITarget(
        parent,
        name,
        0x8086,
        0x1209,
        0x0000,
        0x0000,
        0x10,  // 82551
        0x200000,  // ethernet controller
        0,  // 82551 hardcoded
        0,  // disabled
        0x70,
        0x8086,
        0x0000_0000,
        0x00,
        0,
        1,  // INTA# used
        0xFF,
        0xFF,
        0xd0000000L to 0x1000,  // CSRMap (4 Kb)
        0x0000d001L to 0x20,  // CSRIO (32 bytes)
        0xd0100000L to 0x100000  // flashMemoryMap  (1 Mb)
) {

//        val mdiPhyAddr: Int = 1 // MDI Physical Address

    companion object {
        @Transient private val log = logger(Level.FINE)
    }

    val spi = ports.Master("spi", PIN)

    inner class EEPROM_CONTROL_REG(port: SlavePort, address: Long, name: String) : Register(port, address, WORD, name) {
        override fun read(ea: Long, ss: Int, size: Int): Long = spi.read(ea - address, ss, size)
        override fun write(ea: Long, ss: Int, size: Int, value: Long) = spi.write(ea - address, ss, size, value)
    }

    val MEM_CSR_SCB_SW = Register(ports.pci[PCI_CSR0], 0x00, WORD, "MEM_CSR_SCB_SW")

    val MEM_CSR_SCB_CW = object : ByteAccessRegister(ports.pci[PCI_CSR0], 0x02, WORD, "MEM_CSR_SCB_CW") {
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

        override fun read(ea: Long, ss: Int, size: Int): Long {
            CUS = 0
            RUS = 0
            rsvd = 0
            return super.read(ea, ss, size)
        }
    }

    val MEM_CSR_SCB_GP = Register(ports.pci[PCI_CSR0], 0x04, DWORD, "MEM_CSR_SCB_GP")
    val MEM_CSR_PORT = Register(ports.pci[PCI_CSR0], 0x08, DWORD, "MEM_CSR_PORT")

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
    inner class CSR_MDI_CR(port: SlavePort, address: Long, name: String) : Register(port, address, DWORD, name) {
        var IE by bit(29)  // 29
        var R by bit(28)  // 28
        var Opcode by field(27..26)  // 27..26
        var PHYAdd by field(25..21)  // 25..21
        var RegAdd by field(20..16)  // 20..16
        var Data by field(15..0)

        override fun stringify() = "${super.stringify()} [IE=$IE R=$R Opcode=$Opcode PHYAdd=$PHYAdd RegAdd=$RegAdd data=$Data]"

        override fun read(ea: Long, ss: Int, size: Int): Long {
            R = 1
            return super.read(ea, ss, size)
        }
    }

    val MEM_CSR_MDI_CR = CSR_MDI_CR(ports.pci[PCI_CSR0], 0x10, "MEM_CSR_MDI_CR")

    val MEM_CSR_EEPROM_CR = EEPROM_CONTROL_REG(ports.pci[PCI_CSR0], 0x0E, "MEM_CSR_EEPROM_CR")
    val MEM_CSR_RX_DMA_BC = Register(ports.pci[PCI_CSR0], 0x14, DWORD, "MEM_CSR_RX_DMA_BC")
    val MEM_CSR_FCR = Register(ports.pci[PCI_CSR0], 0x19, WORD, "MEM_CSR_FCR")
    val MEM_CSR_PMDR = Register(ports.pci[PCI_CSR0], 0x1B, BYTE, "MEM_CSR_PMDR")
    val MEM_CSR_GC = Register(ports.pci[PCI_CSR0], 0x1C, BYTE, "MEM_CSR_GC")
    val MEM_CSR_GS = Register(ports.pci[PCI_CSR0], 0x1D, BYTE, "MEM_CSR_GS")
    val MEM_CSR_FER = Register(ports.pci[PCI_CSR0], 0x30, DWORD, "MEM_CSR_FER")
    val MEM_CSR_FEMR = Register(ports.pci[PCI_CSR0], 0x34, DWORD, "MEM_CSR_FEMR")
    val MEM_CSR_FPSR = Register(ports.pci[PCI_CSR0], 0x38, DWORD, "MEM_CSR_FPSR")
    val MEM_CSR_FCRER = Register(ports.pci[PCI_CSR0], 0x3C, DWORD, "MEM_CSR_FCRER")

    val IO_CSR_SCB_SW = Register(ports.pci[PCI_CSR1], 0x00, WORD, "IO_CSR_SCB_SW")
    val IO_CSR_SCB_CW = Register(ports.pci[PCI_CSR1], 0x02, WORD, "IO_CSR_SCB_CW")
    val IO_CSR_SCB_GP = Register(ports.pci[PCI_CSR1], 0x04, DWORD, "IO_CSR_SCB_GP")
    val IO_CSR_PORT = Register(ports.pci[PCI_CSR1], 0x08, DWORD, "IO_CSR_PORT")
    val IO_CSR_EEPROM_CR = EEPROM_CONTROL_REG(ports.pci[PCI_CSR1], 0x0E, "IO_CSR_EEPROM_CR")
    val IO_CSR_MDI_CR = CSR_MDI_CR(ports.pci[PCI_CSR1], 0x10, "IO_CSR_MDI_CR")
    val IO_CSR_RX_DMA_BC = Register(ports.pci[PCI_CSR1], 0x14, DWORD, "IO_CSR_RX_DMA_BC")
    val IO_CSR_FCR = Register(ports.pci[PCI_CSR1], 0x19, WORD, "IO_CSR_FCR")
    val IO_CSR_PMDR = Register(ports.pci[PCI_CSR1], 0x1B, BYTE, "IO_CSR_PMDR")
    val IO_CSR_GC = Register(ports.pci[PCI_CSR1], 0x1C, BYTE, "IO_CSR_GC")
    val IO_CSR_GS = Register(ports.pci[PCI_CSR1], 0x1D, BYTE, "IO_CSR_GS")
    val IO_CSR_FER = Register(ports.pci[PCI_CSR1], 0x30, DWORD, "IO_CSR_FER")
    val IO_CSR_FEMR = Register(ports.pci[PCI_CSR1], 0x34, DWORD, "IO_CSR_FEMR")
    val IO_CSR_FPSR = Register(ports.pci[PCI_CSR1], 0x38, DWORD, "IO_CSR_FPSR")
    val IO_CSR_FCRER = Register(ports.pci[PCI_CSR1], 0x3C, DWORD, "IO_CSR_FCRER")


    // additional PCI function 0 registers
    val CAP_IDENT = PCI_CONF_FUNC_RD(0, 0xDC, BYTE, "CAP_IDENT")
    val NEXT_ITEM_PTR = PCI_CONF_FUNC_RD(0, 0xDD, BYTE, "NEXT_ITEM_PTR")
    val POWER_CAPABILITIES = PCI_CONF_FUNC_RD(0, 0xDE, WORD, "POWER_CAPABILITIES")

    val POWER_MANAGEMENT_CSR = PCI_CONF_FUNC_RD(0, 0xE0, WORD, "POWER_MANAGEMENT_CSR")
    val DATA = PCI_CONF_FUNC_WR(0, 0xE2, BYTE, "DATA")
}