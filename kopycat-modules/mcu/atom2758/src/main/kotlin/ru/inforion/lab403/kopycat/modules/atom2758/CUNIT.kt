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
package ru.inforion.lab403.kopycat.modules.atom2758

import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.logging.CONFIG
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.modules.common.pci.PciBridge

/**
 * Intel Atom Processor C2000 Product Family for Microserver
 * System Agent Register Map, page 106
 */
class CUNIT(parent: Module, name: String) : PciBridge(
    parent, name, 0x8086, 0x1F18, secondaryBusNumber = 1) {

    val msg = ports.Port("msg")  // master access to whole bus

    val CUNIT_MSG_DATA_REG = PCI_CONF_FUNC_WR(0xD4, DWORD, "CUNIT_MSG_DATA_REG", level = FINE)
    val CUNIT_MSG_CTRL_REG_EXT = PCI_CONF_FUNC_WR(0xD8, DWORD, "CUNIT_MSG_CTRL_REG_EXT", level = FINE)
    val CUNIT_MSG_CTRL_PACKET_REG = PCI_CONF_FUNC_WR(0xDC, DWORD, "CUNIT_MSG_CTRL_PACKET_REG", level = CONFIG)
    val CUNIT_MANUFACTURING_ID = PCI_CONF_FUNC_WR(0xF8, DWORD, "CUNIT_MANUFACTURING_ID", level = CONFIG)

    val BAR_10 = PCI_BAR(0x10, DWORD, "BAR_10")
    val BAR_14 = PCI_BAR(0x14, DWORD, "BAR_14")

    // Not in docs
    val UNKNOWN_54 = PCI_CONF_FUNC_WR(0x54, DWORD, "UNKNOWN_54", level = CONFIG)
    val UNKNOWN_64 = PCI_CONF_FUNC_WR(0x64, DWORD, "UNKNOWN_64", level = CONFIG)
    val UNKNOWN_C4 = PCI_CONF_FUNC_WR(0xC4, DWORD, "UNKNOWN_C4", level = CONFIG)
    val UNKNOWN_C8 = PCI_CONF_FUNC_WR(0xC8,  DWORD, "UNKNOWN_C8", level = CONFIG)

    val CUNIT_MDR_SAI = PCI_CONF_FUNC_WR(0x118, DWORD, "CUNIT_MDR_SAI", 0u, level = CONFIG)
    val UNKNOWN_12C = PCI_CONF_FUNC_WR(0x12C, DWORD, "UNKNOWN_12C", 0u, level = CONFIG)
    val UNKNOWN_198 = PCI_CONF_FUNC_WR(0x198, DWORD, "UNKNOWN_198", 0u, level = CONFIG)
    val UNKNOWN_1AC = PCI_CONF_FUNC_WR(0x1AC, DWORD, "UNKNOWN_1AC", 0u, level = CONFIG)
    val UNKNOWN_218 = PCI_CONF_FUNC_WR(0x218, DWORD, "UNKNOWN_218", 0u, level = CONFIG)
    val UNKNOWN_22C = PCI_CONF_FUNC_WR(0x22C, DWORD, "UNKNOWN_22С", 0u, level = CONFIG)
    val UNKNOWN_298 = PCI_CONF_FUNC_WR(0x298, DWORD, "UNKNOWN_298", 0u, level = CONFIG)
    val UNKNOWN_2AC = PCI_CONF_FUNC_WR(0x2AC, DWORD, "UNKNOWN_2AC", 0u, level = CONFIG)

    val CUNIT_MSG_CTRL_REG = object : PCI_CONF_FUNC_WR(0xD0, DWORD, "CUNIT_MSG_CTRL_REG", level = CONFIG) {
        val MESSAGE_OPCODE by field(31..24)
        val MESSAGE_PORT by field(23..16)
        val MESSAGE_ADDRESS_OFFSET by field(15..8)
        val MESSAGE_WR_BYTE_ENABLES by field(7..4)
        val RESERVED by field(3..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)

            // Intel Atom Processor E3800 Product Family Datasheet
            // 13.4.6 CUNIT_MSG_CTRL_REG—Offset D0h, page 348
            val opcode = MESSAGE_OPCODE
            val port = MESSAGE_PORT
            val offset = MESSAGE_ADDRESS_OFFSET
            val ext = CUNIT_MSG_CTRL_REG_EXT.data
            val wrSize = MESSAGE_WR_BYTE_ENABLES.countOneBits()

            CUNIT_MSG_CTRL_REG_EXT.data = 0u  // required hardware side cleanup

            val operation = msg.requestOperationType(port, opcode.int)

            val address = messageBusAddress(port, ext, offset)

            when (operation) {
                MESSAGE_BUS_READ_OPERATION -> {
                    val result = if (msg.access(address, -1, wrSize)) {
                        msg.read(address, -1, wrSize).also {
                            log.config { "[0x${core.pc.hex}] Read from message bus port=$port offset=0x${(ext or offset).hex8} value=0x${it.hex}" }
                        }
                    } else {
                        log.warning { "[0x${core.pc.hex}] Can't access on read to message bus port=$port offset=${(ext or offset).hex8}" }
                        0u
                    }
                    CUNIT_MSG_DATA_REG.data = result
                }

                MESSAGE_BUS_WRITE_OPERATION -> {
                    val data = CUNIT_MSG_DATA_REG.data
                    if (msg.access(address, -1, wrSize)) {
                        msg.write(address, -1, wrSize, data).also {
                            log.config { "[0x${core.pc.hex}] Write to message bus port=$port offset=0x${(ext or offset).hex8} value=0x${data.hex}" }
                        }
                    } else {
                        log.warning { "[0x${core.pc.hex}] Can't access on write to message bus port=$port offset=0x${(ext or offset).hex8} value=0x${data.hex8}" }
                    }
                }

                MESSAGE_BUS_UNKNOWN_OPERATION -> {
                    log.severe { "[0x${core.pc.hex}] Device on message bus port=$port try to execute unknown opcode 0x${opcode.hex}" }
                    CUNIT_MSG_DATA_REG.data = 0u
                }

                MESSAGE_BUS_DEVICE_NOT_SUPPORTED -> {
                    log.severe { "[0x${core.pc.hex}] Device on message bus port=$port service point not found at 0x${MESSAGE_BUS_SERVICE_ADDR.hex} set CUNIT_MSG_DATA_REG to 0" }
                    CUNIT_MSG_DATA_REG.data = 0u
                }

                else -> error("[0x${core.pc.hex}] CUNIT Device on message bus port=$port return unknown operation type=0x${operation.hex}")
            }
        }
    }
}
