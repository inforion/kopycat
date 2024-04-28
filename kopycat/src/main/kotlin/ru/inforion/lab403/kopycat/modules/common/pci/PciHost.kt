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
package ru.inforion.lab403.kopycat.modules.common.pci

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINER
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.SlavePort
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.LOAD
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.STORE
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.modules.*


open class PciHost constructor(parent: Module, name: String): Module(parent, name) {
    companion object {
        @Transient val log = logger(FINER)

        const val INTERRUPT_COUNT = 4
    }

    inner class Ports : ModulePorts(this) {
        val io = Slave("io", BUS16)

        val pci = pci_master("pci")
    }

    final override val ports = Ports()

    protected inner class PCI_CFG_ADR_REG(port: SlavePort, address: ULong, name: String) :
            ByteAccessRegister(port, address, DWORD, name) {

        val enabled by bit(PCI_BDF_ENA_BIT)
        val bus by field(PCI_BDF_BUS_RANGE)
        val device by field(PCI_BDF_DEVICE_RANGE)
        val func by field(PCI_BDF_FUNC_RANGE)
        val reg by field(PCI_BDF_REG_RANGE)  // last two bits should be always = 0

//        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
//            super.write(ea, ss, size, value)
//            log.severe { "[io=0x${ea.hex4} sz=$size] Setup PCI device address: enabled=$enabled bus=$bus device=$device func=$func reg=$reg" }
//        }
    }

    protected inner class PCI_CFG_DAT_REG(port: SlavePort, address: ULong, name: String) :
            ByteAccessRegister(port, address, DWORD, name) {

        // TODO: may be last two bits of reg should be 0 from I/O port
        private fun getAddress() = PciConfigAddress.fromBusFuncDeviceReg(PCICFGADR.data and 0xFFFF_FFFCuL)

        // if addr is not 0xcfc, the offset is moved accordingly
        // https://github.com/cristim/coreboot/blob/master/src/devices/oprom/yabel/io.c line 455

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            val pci = getAddress()
            val off = offset(ea)

            return if (pci.enabled) {
                // it's a bit slower because find called twice but PCI should return ff's when nothing connected
                if (!ports.pci.access(pci.offset + off, ss, size, LOAD)) {
                    log.warning { "$pci: Read $size from $this off=0x${off.hex} -> 0x${PCI_NOTHING_CONNECTED.hex8} : Nothing connected" }
                    PCI_NOTHING_CONNECTED
                } else {
                    val data = ports.pci.read(pci.offset + off, ss, size)
                    log.finer { "$pci: Read $size from $this off=0x${off.hex} -> value=${data.hex8}" }
                    data
                }
            } else {
                log.finer { "[io=0x${ea.hex4} sz=$size] Read disabled $pci off=$off" }
                PCI_NOTHING_CONNECTED
            }
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val pci = getAddress()
            val off = offset(ea)

            if (pci.enabled) {
                if (!ports.pci.access(pci.offset + off, ss, size, STORE)) {
                    log.debug { "$pci: Write $size to $pci off=0x${off.hex} -> value=0x${value.hex8} : Nothing connected" }
                } else {
                    log.finer { "$pci: Write $size to $this off=0x${off.hex} -> value=0x${value.hex8}" }
                    ports.pci.write(pci.offset + off, ss, size, value)
                }
            } else {
                log.warning { "[io=0x${ea.hex4} sz=$size] Write disabled $pci off=$off" }
            }
        }
    }

    protected val PCICFGADR = PCI_CFG_ADR_REG(ports.io, 0xCF8u, "PCICFGADR")  // Byte accessed Dword size
    protected val PCICFGDATA = PCI_CFG_DAT_REG(ports.io, 0xCFCu, "PCICFGDATA")  // Byte accessed Dword size
}