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
package ru.inforion.lab403.kopycat.modules.common.pci

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.*
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.extensions.mapOffset
import ru.inforion.lab403.kopycat.cores.base.extensions.unmap
import ru.inforion.lab403.kopycat.modules.*
import ru.inforion.lab403.kopycat.serializer.loadValue
import ru.inforion.lab403.kopycat.serializer.storeValues
import java.util.logging.Level
import java.util.logging.Level.*

@Suppress("unused", "PropertyName", "ClassName")

abstract class PciAbstract(
    parent: Module,
    name: String,
    vendorId: Int,
    deviceId: Int,
    revisionId: Int = 0,
    classCode: Int = 0,
    headerType: Int = 0,
    bist: Int = 0,
) : Module(parent, name) {

    companion object {
        @Transient val log = logger(FINE)
    }

    inner class Ports : ModulePorts(this) {
        val pci = pci_slave("pci")

        val mapper = Master("mapper")
    }

    final override val ports = Ports()

    protected lateinit var myAddress: PciConfigAddress

    override fun onPortConnected(port: APort, bus: Bus, offset: ULong) {
        if (port != ports.pci) return

        val address = PciConfigAddress.fromOffset(offset)

        if (::myAddress.isInitialized) {
            require(myAddress.bus == address.bus) { "PCI target $this has different bus=${address.bus}" }
            require(myAddress.device == address.device) { "PCI target $this has different device=${address.device}" }
            require(myAddress.func == address.func) { "PCI target $this has different func=${address.func}" }
        } else {
            myAddress = address
            log.warning { "$this connected to $myAddress" }
        }
    }

    private var ioSpaceEnabled = false
    private var memSpaceEnabled = false

    open inner class PCI_BAR constructor(
        reg: Int,
        datatype: Datatype,
        name: String,
        val range: Int = 0,
        val area: Int = PCI_UNDEF_AREA,
        val index: Int = -1,
        level: Level = FINER
    ) : ByteAccessRegister(ports.pci, reg.ulong_z, datatype, name, level = level) {
        internal var sizeRequested = false
        internal var base: ULong = 0u
        // Align to power of two
        private val alignedSize = range.takeHighestOneBit().let { highestOne ->
            if (range == highestOne) {
                range
            } else {
                highestOne shl 1
            }
        }

        private val configBitsCount = when (area) {
            PCI_MEM_AREA -> 4
            PCI_IO_AREA -> 1
            PCI_UNDEF_AREA -> 0
            else -> error("Wrong area type for PCI_BAR = $area")
        }

        private val spaceSizeRequestValue = ULONG_MAX like datatype

        private val locationType = when (datatype) {
            WORD, DWORD -> 0
            QWORD -> 2
            else -> error("PCI_BAR register can be only 64-biy, 32-bit length or 16-bit length")
        }

        private val isSpaceEnabled get() = when (area) {
            PCI_MEM_AREA -> memSpaceEnabled
            PCI_IO_AREA -> ioSpaceEnabled
            PCI_UNDEF_AREA -> false
            else -> error("Wrong area type for PCI_BAR = $area")
        }

        internal val isBaseAddressInvalid get() = base.untruth

        internal val isBaseAddressValid get() = !isBaseAddressInvalid

        private val isSpaceSizeRequested get() = data == spaceSizeRequestValue

        private fun clearConfigBits(value: ULong) = value mask datatype.msb..configBitsCount

        private fun setConfigBits(value: ULong) = clearConfigBits(value)
            .insert(area, 0)
            .insert(locationType.ulong_z, 2..1)
            .insert(0, 3)

        internal fun map() = ports.mapper.mapOffset(this@PCI_BAR.name, base, alignedSize, area, index)

        internal fun unmap() = ports.mapper.unmap(this@PCI_BAR.name, area, index)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            data = if (sizeRequested) {
                sizeRequested = false
                setConfigBits(-alignedSize.ulong_z)
            } else {
                setConfigBits(base)
            }

            return super.read(ea, ss, size)
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)

            sizeRequested = false

            base = clearConfigBits(data)

            if (isSpaceEnabled) {
                if (isBaseAddressInvalid) unmap() else map()
            } else if (isSpaceSizeRequested){
                sizeRequested = true
            }
        }

        init {
            require(datatype == QWORD || datatype == DWORD || datatype == WORD) {
                "PCI_BAR register can be only 64-biy, 32-bit length or 16-bit length"
            }

            bars.add(this)
        }
    }

    private val bars = mutableListOf<PCI_BAR>()

    open inner class PCI_CONF_FUNC(
        reg: Int,
        datatype: Datatype,
        name: String,
        default: ULong = 0u,
        writable: Boolean,
        level: Level
    ) : ByteAccessRegister(ports.pci, reg.ulong_z, datatype, name, default, writable, level = level) {
        /**
         * Convert this offset (reg) to full PCI device address for this target
         */
        private fun ULong.toPCIDeviceAddress() =
            PciConfigAddress.fromBusFuncDeviceReg(myAddress.bdf.insert(this, PCI_BDF_REG_RANGE))

        override fun stringify() = "${address.toPCIDeviceAddress()} $name data=0x${data.hex8}"
    }

    open inner class PCI_CONF_FUNC_RD(reg: Int, datatype: Datatype, name: String, default: ULong = 0u, level: Level = CONFIG) :
            PCI_CONF_FUNC(reg, datatype, name, default, false, level)

    open inner class PCI_CONF_FUNC_WR(reg: Int, datatype: Datatype, name: String, default: ULong = 0u, level: Level = CONFIG) :
            PCI_CONF_FUNC(reg, datatype, name, default, true, level)

    // read simultaneously two registers
    val VENDOR_DEVICE_ID = object : PCI_CONF_FUNC_WR(0x00, DWORD, "VENDOR_DEVICE_ID", level = WARNING) {
        var DEVICE_ID by field(31..16)
        var VENDOR_ID by field(15..0)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            VENDOR_ID = vendorId.ulong_z
            DEVICE_ID = deviceId.ulong_z
            return super.read(ea, ss, size)
        }
    }

    internal val capabilities = mutableListOf<PciCapability>()

    inner class COMMAND_STATUS_CLASS : PCI_CONF_FUNC_WR(0x04, DWORD, "COMMAND_STATUS", level = SEVERE) {

        var STATUS by field(31..16)
        var COMMAND by field(15..0)

        /** Capabilities List */
        var CL by bit(20)

        /** Interrupt Status */
        var IS by bit(19)

        /** Interrupt Disable. Does not affect MSI. */
        var ID by bit(10)

        /** Bus Master Enable, rw */
        var BME by bit(2)

        /** Controls access to the memory space, rw */
        var MSE by bit(1)

        /** Controls access to the I/O space, rw */
        var IOSE by bit(0)

        private fun areaBars(area: Int) = bars.filter { it.area == area }

        fun remap(oldValue: Boolean, newValue: Boolean, area: Int): Boolean {
            when {
                // we should map only this register which has new addresses
                !oldValue && newValue -> areaBars(area)
                    .filter { it.isBaseAddressValid }
                    .forEach { it.map() }
                oldValue && !newValue -> areaBars(area)
                    .forEach { it.unmap() }
            }

            return newValue
        }

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            CL = capabilities.isNotEmpty().int
            return super.read(ea, ss, size)
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            memSpaceEnabled = remap(memSpaceEnabled, MSE.truth, PCI_MEM_AREA)
            ioSpaceEnabled = remap(ioSpaceEnabled, IOSE.truth, PCI_IO_AREA)
        }
    }

    val COMMAND_STATUS = COMMAND_STATUS_CLASS()

    val REVISION_ID_CLASS_CODE = object : PCI_CONF_FUNC_WR(0x08, DWORD, "REVISION_ID_CLASS_CODE", level = WARNING) {
        var CLASS_CODE by field(31..8)
        var REVISION_ID by field(7..0)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            REVISION_ID = revisionId.ulong_z
            CLASS_CODE = classCode.ulong_z
            return super.read(ea, ss, size)
        }
    }

    // CACHE_LINE, LATENCY_TIMER, HEADER_TYPE, BIST
    val PARAMETERS_0C = object : PCI_CONF_FUNC_WR(0x0C, DWORD, "PARAMETERS_0C") {
        var BIST by field(31..24)
        var HEADER_TYPE by field(23..16)
        var LATENCY_TIMER by field(15..8)
        var CACHE_LINE by field(7..0)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            HEADER_TYPE = headerType.ulong_z
            BIST = bist.ulong_z
            return super.read(ea, ss, size)
        }
    }

    val TEST = PCI_CONF_FUNC_WR(0xFF, BYTE, "TEST")

    override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + storeValues(
        "bars" to bars.associate {
            it.name to storeValues(
                "sizeRequested" to it.sizeRequested,
                "base" to it.base
            )
        }
    )

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)

        val data = snapshot["bars"].cast<Map<String, Any>>()

        bars.forEach { bar ->
            data[bar.name] ifItNotNull {
                val values = it.cast<Map<String, Any>>()
                bar.sizeRequested = loadValue(values, "sizeRequested")
                bar.base = loadValue(values, "base")
            } otherwise {
                log.severe { "PCI_BAR ${bar.name} not found in snapshot" }
            }
        }
    }
}