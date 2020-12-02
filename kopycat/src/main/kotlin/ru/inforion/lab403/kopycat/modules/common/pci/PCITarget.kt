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

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.APort
import ru.inforion.lab403.kopycat.cores.base.Bus
import ru.inforion.lab403.kopycat.cores.base.SlavePort
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.modules.PCI_CONF
import ru.inforion.lab403.kopycat.modules.PCI_REQUEST_SPACE_SIZE
import ru.inforion.lab403.kopycat.modules.PCI_SPACES_COUNT
import java.util.logging.Level

@Suppress("unused", "PropertyName", "ClassName")

abstract class PCITarget(
        parent: Module,
        name: String,
        vendorId: Int,
        deviceId: Int,
        command: Int,
        status: Int,
        revisionId: Int,
        classCode: Int,
        headerType: Int,
        bist: Int,
        subSystemId: Int,
        subVendorId: Int,
        expRomBase: Int,
        capPtr: Int,
        intLine: Int,
        intPin: Int,
        minGrant: Int,
        minLatency: Int,
        vararg spaces: Pair<Long, Int>
) : Module(parent, name) {

    companion object {
        @Transient private val log = logger(Level.FINE)
    }

    inner class Ports : ModulePorts(this) {
        val pci = pci_slave("pci")

        val map = Master("map")
    }

    final override val ports = Ports()

    data class Space(val id: Int, var base: Long = 0, val size: Int = 0)

    val spaces = Array(PCI_SPACES_COUNT) { k ->
        if (k < spaces.size) Space(k, spaces[k].first, spaces[k].second) else Space(k)
    }

    private lateinit var myAddress: PCIAddress

    private fun remap(id: Int) = ports.map.write(spaces[id].base, myAddress.bdf, spaces[id].size, id.asULong)

    override fun onPortConnected(port: APort, bus: Bus, offset: Long) {
        if (port !is SlavePort)
            return

        val address = PCIAddress.fromPrefix(offset)

        if (::myAddress.isInitialized) {
            require(myAddress.bus == address.bus) { "PCI target $this has different bus=${address.bus} for space=$port" }
            require(myAddress.device == address.device) { "PCI target $this has different device=${address.device} for space=$port" }
        } else {
            myAddress = address
            log.warning { "$this connected to $myAddress" }
        }
    }

    override fun reset() {
        super.reset()
        spaces.forEach { remap(it.id) }
    }

    open fun readFunc0Command(offset: Int): Long = 0
    open fun writeFunc0Command(offset: Int, value: Long) = Unit

    open fun readFunc0Status(offset: Int): Long = 0
    open fun writeFunc0Status(offset: Int, value: Long) = Unit

    open inner class PCI_CONF_FUNC(func: Int, reg: Int, datatype: Datatype, name: String, default: Int = 0, writable: Boolean = true) :
            ByteAccessRegister(
                    ports.pci[PCI_CONF], pciFuncRegPrefix(func, reg),
                    datatype, name, default.asULong, writable, level = Level.FINEST)

    open inner class PCI_CONF_FUNC_RD(func: Int, reg: Int, datatype: Datatype, name: String, default: Int = 0) :
            PCI_CONF_FUNC(func, reg, datatype, name, default, false)

    open inner class PCI_CONF_FUNC_WR(func: Int, reg: Int, datatype: Datatype, name: String, default: Int = 0) :
            PCI_CONF_FUNC(func, reg, datatype, name, default, true)

    inner class PCI_CONF_FUNC_SPACE(func: Int, val space: Int, name: String) :
            PCI_CONF_FUNC_WR(func, 0x10 + space * 4, DWORD, name, 0) {

        private val spaceSizeRequested = Array(PCI_SPACES_COUNT) { false }

        override fun read(ea: Long, ss: Int, size: Int): Long {
            val data = if (spaceSizeRequested[space]) {
                spaceSizeRequested[space] = false
                val tmp = -spaces[space].size
                val bit = ioSpaceBit(spaces[space].base)
                (tmp or bit).asULong
            } else {
                spaces[space].base
            }

            log.fine { "$name pci-reading offset=0x${ea.hex} value=0x${data.hex} size=$size" }
            return data
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            log.fine { "$name pci-writing offset=0x${ea.hex} value=0x${value.hex} size=$size" }

            if (value != PCI_REQUEST_SPACE_SIZE) {
                spaces[space].base = value
                remap(space)
            } else {
                spaceSizeRequested[space] = true
            }
        }
    }

    // read simultaneously two registers
    val VENDOR_DEVICE_ID = PCI_CONF_FUNC_RD(0, 0x00, DWORD,
            "VENDOR_DEVICE_ID", vendorId or (deviceId shl 16))

//    val DEVICE_ID = PCI_CONF_FUNC_RD(0, 0x04, WORD, "DEVICE_ID", deviceId)
    val COMMAND = object : PCI_CONF_FUNC_WR(0, 0x04, WORD, "COMMAND", command) {
        override fun read(ea: Long, ss: Int, size: Int) = readFunc0Command(ea.asInt)
        override fun write(ea: Long, ss: Int, size: Int, value: Long) = writeFunc0Command(ea.asInt, value)
    }
    val STATUS = object : PCI_CONF_FUNC_WR(0, 0x06, WORD, "STATUS", status) {
        override fun read(ea: Long, ss: Int, size: Int) = readFunc0Status(ea.asInt)
        override fun write(ea: Long, ss: Int, size: Int, value: Long) = writeFunc0Status(ea.asInt, value)
    }

    val REVISION_ID = PCI_CONF_FUNC_RD(0, 0x08, BYTE, "REVISION_ID", revisionId)
    val CLASS_CODE = PCI_CONF_FUNC_RD(0, 0x09, TRIBYTE, "CLASS_CODE", classCode)

    val CACHE_LINE = PCI_CONF_FUNC_WR(0, 0x0C, BYTE, "CACHE_LINE")
    val LATENCY_TIMER = PCI_CONF_FUNC_WR(0, 0x0D, BYTE, "LATENCY_TIMER")
    val HEADER_TYPE = PCI_CONF_FUNC_RD(0, 0x0E, BYTE, "HEADER_TYPE", headerType)
    val BIST = PCI_CONF_FUNC_WR(0, 0x0F, BYTE, "BIST", bist)

    val SPACE_BASE_0 = PCI_CONF_FUNC_SPACE(0, 0, "SPACE_BASE_0")
    val SPACE_BASE_1 = PCI_CONF_FUNC_SPACE(0, 1, "SPACE_BASE_1")
    val SPACE_BASE_2 = PCI_CONF_FUNC_SPACE(0, 2, "SPACE_BASE_2")
    val SPACE_BASE_3 = PCI_CONF_FUNC_SPACE(0, 3, "SPACE_BASE_3")
    val SPACE_BASE_4 = PCI_CONF_FUNC_SPACE(0, 4, "SPACE_BASE_4")
    val SPACE_BASE_5 = PCI_CONF_FUNC_SPACE(0, 5, "SPACE_BASE_5")
    val SPACE_BASE_6 = PCI_CONF_FUNC_SPACE(0, 6, "SPACE_BASE_6")

    val SUB_VENDOR_ID = PCI_CONF_FUNC_RD(0, 0x2C, WORD, "SUB_VENDOR_ID", subVendorId)
    val SUB_SYSTEM_ID = PCI_CONF_FUNC_RD(0, 0x2E, WORD, "SUB_SYSTEM_ID", subSystemId)

    val EXP_ROM_BASE = PCI_CONF_FUNC_RD(0, 0x30, DWORD, "EXP_ROM_BASE", expRomBase)

    val CAP_PTR = PCI_CONF_FUNC_RD(0, 0x34, BYTE, "CAP_PTR", capPtr)

    val INT_LINE = PCI_CONF_FUNC_RD(0, 0x3C, BYTE, "INT_LINE", intLine)
    val INT_PIN = PCI_CONF_FUNC_RD(0, 0x3D, BYTE, "INT_PIN", intPin)
    val MIN_GRANT = PCI_CONF_FUNC_WR(0, 0x3E, BYTE, "MIN_GRANT", minGrant)
    val MAX_LATENCY = PCI_CONF_FUNC_WR(0, 0x3F, BYTE, "MAX_LATENCY", minLatency)

    val TEST = PCI_CONF_FUNC_WR(7, 0xFF, BYTE, "TEST")
}