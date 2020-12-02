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
package ru.inforion.lab403.kopycat.modules.common.pci

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.MasterPort
import ru.inforion.lab403.kopycat.cores.base.SlavePort
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.LOAD
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.STORE
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.interfaces.IFetchReadWrite
import ru.inforion.lab403.kopycat.modules.*
import java.util.logging.Level.FINER


open class PCIHost(parent: Module, name: String): Module(parent, name) {
    companion object {
        @Transient val log = logger(FINER)

        const val INTERRUPT_COUNT = 4
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", BUS32)
        val io = Slave("io", BUS16)

        val mmcr = Slave("mmcr", BUS12)

        val pci = pci_master("pci")
        val map = Slave("map")
    }

    final override val ports = Ports()

    data class Region(val port: MasterPort, val bus: Int, val device: Int, val range: LongRange) : IFetchReadWrite {
        private val busOffset = pciBusDevicePrefix(bus, device)

        override fun fetch(ea: Long, ss: Int, size: Int): Long =
                port.fetch(ea - range.first + busOffset, ss, size)
        override fun read(ea: Long, ss: Int, size: Int) =
                port.read(ea - range.first + busOffset, ss, size)
        override fun write(ea: Long, ss: Int, size: Int, value: Long) =
                port.write(ea - range.first + busOffset, ss, size, value)
    }

    protected inner class PCI_SPACE_AREA(port: SlavePort, size: Long, name: String) :
            Area(port, 0, size - 1, name) {

        private var region: Region? = null
        private val mapping = mutableListOf<Region>()

        override fun beforeRead(from: MasterPort, ea: Long): Boolean {
            region = mapping.find { ea inside it.range }
            return region != null
        }

        override fun beforeWrite(from: MasterPort, ea: Long, value: Long): Boolean {
            region = mapping.find { ea inside it.range }
            return region != null
        }

        override fun fetch(ea: Long, ss: Int, size: Int) = region!!.fetch(ea, ss, size)
        override fun read(ea: Long, ss: Int, size: Int) = region!!.read(ea, ss, size)
        override fun write(ea: Long, ss: Int, size: Int, value: Long) = region!!.write(ea, ss, size, value)

        fun configureMapping(bus: Int, device: Int, base: Long, size: Int, targetSpace: Int) {
            val port = ports.pci[targetSpace]

            mapping.removeIf { bus == it.bus && device == it.device && port == it.port }

            if (size != 0) {
                val alignedBase = base and 0xFFFF_FFFFE  // remove IO bit if set
                val range = alignedBase until alignedBase + size

                val failed = mapping.filter { range isIntersect it.range }
                require(failed.isEmpty()) {
                    val str = failed.joinToString("\n ")
                    "[$range] has intersections with:\n$str"
                }

                val region = Region(port, bus, device, range)

                mapping.add(region)

                log.warning { "$this range 0x${range.hex8} map to bus=$bus device=$device space=$port" }
            }
        }
    }

    private val HOST_MEM_AREA = PCI_SPACE_AREA(ports.mem, BUS32, "HOST_MEM_SPACE")
    private val HOST_IO_AREA = PCI_SPACE_AREA(ports.io, BUS16, "HOST_IO_SPACE")

    protected inner class PCI_CFG_ADR_REG(port: SlavePort, address: Long, name: String) :
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

    protected inner class PCI_CFG_DAT_REG(port: SlavePort, address: Long, name: String) :
            ByteAccessRegister(port, address, DWORD, name) {

        private val output = ports.pci[PCI_CONF]

        private fun getAddress() = PCIAddress.fromBDF(PCICFGADR.data.asInt)

        // if addr is not 0xcfc, the offset is moved accordingly
        // https://github.com/cristim/coreboot/blob/master/src/devices/oprom/yabel/io.c line 455

        override fun read(ea: Long, ss: Int, size: Int): Long {
            val pci = getAddress()
            val off = offset(ea)
            val addr = pciBusDevicePrefix(pci.bus, pci.device) + pciFuncRegPrefix(pci.func, pci.reg) + off

            return if (pci.enabled) {
                // it's a bit slower because find called twice but PCI should return ff's when nothing connected
                if (!output.access(addr, ss, size, LOAD)) {
//                    log.warning { "[io=0x${ea.hex4} sz=$size] Nothing connected to $pci off=$off -> 0x${PCI_NOTHING_CONNECTED.hex8}" }
                    PCI_NOTHING_CONNECTED
                } else {
                    val data = output.read(addr, ss, size)
                    log.finer { "[io=0x${ea.hex4} sz=$size] Read $pci off=$off -> value=${data.hex8}" }
                    data
                }
            } else {
                log.finer { "[io=0x${ea.hex4} sz=$size] Read disabled $$pci off=$off" }
                PCI_NOTHING_CONNECTED
            }
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            val pci = getAddress()
            val off = offset(ea)
            val addr = pciBusDevicePrefix(pci.bus, pci.device) + pciFuncRegPrefix(pci.func, pci.reg) + off

            if (pci.enabled) {
                // See read callback (for symmetry in logging)
                if (!output.access(addr, ss, size, STORE)) {
//                    log.warning { "[io=0x${ea.hex4} sz=$size] Nothing connected to $pci off=$off" }
                } else {
                    log.finer { "[io=0x${ea.hex4} sz=$size] Write $pci off=$off -> value=0x${value.hex8}" }
                    output.write(addr, ss, size, value)
                }
            } else {
                log.warning { "[io=0x${ea.hex4} sz=$size] Write disabled $pci off=$off" }
            }
        }
    }

    protected val PCICFGADR = PCI_CFG_ADR_REG(ports.io, 0xCF8, "PCICFGADR")  // Byte accessed Dword size
    protected val PCICFGDATA = PCI_CFG_DAT_REG(ports.io, 0xCFC, "PCICFGDATA")  // Byte accessed Dword size

    private val REMAP_AREA = object : Area(ports.map, 0, BUS32 - 1, "REMAP_AREA") {
        override fun fetch(ea: Long, ss: Int, size: Int) = throw IllegalAccessException("$name may not be fetched!")
        override fun read(ea: Long, ss: Int, size: Int) = throw IllegalAccessException("$name may not be read!")

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            val pci = PCIAddress.fromBDF(ss)
            val targetSpace = value.asInt
            val hostArea = if (isIOSpace(ea)) HOST_IO_AREA else HOST_MEM_AREA
            hostArea.configureMapping(pci.bus, pci.device, ea, size, targetSpace)
        }
    }
}