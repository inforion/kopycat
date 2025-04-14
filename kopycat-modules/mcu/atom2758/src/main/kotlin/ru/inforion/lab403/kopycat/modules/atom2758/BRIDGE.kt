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

import ru.inforion.lab403.common.extensions.ULONG_MAX
import ru.inforion.lab403.common.extensions.hex16
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessPciError
import ru.inforion.lab403.kopycat.cores.base.extensions.BRIDGE_IO_BUS_INDEX
import ru.inforion.lab403.kopycat.cores.base.extensions.BRIDGE_MEM_BUS_INDEX
import ru.inforion.lab403.kopycat.cores.base.extensions.PCI_ECAM_BUS_INDEX
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.modules.*
import ru.inforion.lab403.kopycat.modules.common.e1000.E1000
import ru.inforion.lab403.kopycat.modules.common.sata.SATA
import ru.inforion.lab403.kopycat.modules.common.pci.PciAddress

class BRIDGE(
    parent: Module,
    name: String,
    val memorySize: ULong = BUS64,
    val ioSize: ULong = BUS16
) : Module(parent, name), IAutoSerializable {

    companion object {
        const val MEMORY_AREA = PCI_MEM_AREA
        const val IO_AREA = PCI_IO_AREA

        const val SATA_BUS_MEM_INDEX_2 = 15
        const val SATA_BUS_MEM_INDEX_3 = 16
        const val E1000_BUS_MEM_INDEX = 17
    }

    inner class Ports : ModulePorts(this) {
        val mem_in = Port("mem_in")
        val io_in = Port("io_in")

        val mem_out = Port("mem_out")
        val io_out = Port("io_out")

        val mapper = Port("mapper")

        val acpi = Port("acpi")
        val pmc = Port("pmc")
        val gpio = Port("gpio")
        val ioc = Port("ioc")
        val ilb = Port("ilb")
        val spi = Port("spi")
        val mphy = Port("mphy")
        val punit = Port("punit")
        val rcrb = Port("rcrb")

        val smb_mem = Port("smb_mem")
        val smb_io = Port("smb_io")

        val smb20_0_mem = Port("smb20_0_mem")
        val smb20_1_mem = Port("smb20_1_mem")

        val usb20 = Port("usb20")

        val sata2 = Port("sata2")
        val sata3 = Port("sata3")

        val e1000 = Port("e1000")

        

        inner class Ecam() : Port("ecam") {
            override fun fetch(ea: ULong, ss: Int, size: Int): ULong = try {
                super.fetch(ea, ss, size)
            } catch (e: MemoryAccessError) {
                throw MemoryAccessPciError(
                    ULONG_MAX, ea, AccessAction.FETCH, "Nothing connected at $ss:${ea.hex16} port $this: ",
                    PciAddress.fromBusFuncDeviceReg(ea)
                )
            }

            override fun read(ea: ULong, ss: Int, size: Int): ULong = try {
                super.read(ea, ss, size)
            } catch (e: MemoryAccessError) {
                throw MemoryAccessPciError(
                    ULONG_MAX, ea, AccessAction.LOAD, "Nothing connected at $ss:${ea.hex16} port $this: ",
                    PciAddress.fromBusFuncDeviceReg(ea)
                )
            }

            override fun write(ea: ULong, ss: Int, size: Int, value: ULong): Unit = try {
                super.write(ea, ss, size, value)
            } catch (e: MemoryAccessError) {
                throw MemoryAccessPciError(
                    ULONG_MAX, ea, AccessAction.STORE, "Nothing connected at $ss:${ea.hex16} port $this: ",
                    PciAddress.fromBusFuncDeviceReg(ea)
                )
            }
        }

        val ecam = Ecam()
    }

    @DontAutoSerialize
    override val ports = Ports()

    @DontAutoSerialize
    private val outputs = mapOf(
        BRIDGE_MEM_BUS_INDEX to ports.mem_out,
        BRIDGE_IO_BUS_INDEX to ports.io_out,

        ACPI.BUS_INDEX to ports.acpi,
        PMC.BUS_INDEX to ports.pmc,
        GPIO.BUS_INDEX to ports.gpio,
        IOC.BUS_INDEX to ports.ioc,
        ILB.BUS_INDEX to ports.ilb,
        SPI.BUS_INDEX to ports.spi,
        MPHY.BUS_INDEX to ports.mphy,
        PUNIT.BUS_INDEX to ports.punit,
        RCRB.BUS_INDEX to ports.rcrb,

        SMB_PCU.BUS_MEM_INDEX to ports.smb_mem,
        SMB_PCU.BUS_IO_INDEX to ports.smb_io,

        SMB_20.BUS_MEM_INDEX_FUNC0 to ports.smb20_0_mem,
        SMB_20.BUS_MEM_INDEX_FUNC1 to ports.smb20_1_mem,

        USB20.BUS_MEM_INDEX to ports.usb20,

        SATA_BUS_MEM_INDEX_2 to ports.sata2,
        SATA_BUS_MEM_INDEX_3 to ports.sata3,

        E1000_BUS_MEM_INDEX to ports.e1000,
        

        PCI_ECAM_BUS_INDEX to ports.ecam
    )

    private val memory = MappingArea(ports.mem_in,"MEMORY", memorySize - 1u, outputs, BRIDGE_MEM_BUS_INDEX)
    private val io = MappingArea(ports.io_in, "IO", ioSize - 1u, outputs, BRIDGE_IO_BUS_INDEX)

    @DontAutoSerialize
    private val mappings = mapOf(MEMORY_AREA to memory, IO_AREA to io)

    private val mapper = Mapper(ports.mapper, "REMAP", BUS32 - 1u, mappings)

    override fun serialize(ctxt: GenericSerializer) = super<IAutoSerializable>.serialize(ctxt)

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) =
        super<IAutoSerializable>.deserialize(ctxt, snapshot)

    override fun stringify() = mappings.values.joinToString("\n") { it.stringify() }
}