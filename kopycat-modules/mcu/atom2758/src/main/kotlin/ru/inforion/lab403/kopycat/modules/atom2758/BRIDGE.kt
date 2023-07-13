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
import ru.inforion.lab403.kopycat.modules.atom2758.e1000.E1000
import ru.inforion.lab403.kopycat.modules.atom2758.sata.SATA
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
    }

    inner class Ports : ModulePorts(this) {
        val mem_in = Slave("mem_in", memorySize)
        val io_in = Slave("io_in", ioSize)

        val mem_out = Master("mem_out", memorySize)
        val io_out = Master("io_out", ioSize)

        val mapper = Slave("mapper")

        val acpi = Master("acpi", ACPI.BUS_SIZE)
        val pmc = Master("pmc", PMC.BUS_SIZE)
        val gpio = Master("gpio", GPIO.BUS_SIZE)
        val ioc = Master("ioc", IOC.BUS_SIZE)
        val ilb = Master("ilb", ILB.BUS_SIZE)
        val spi = Master("spi", SPI.BUS_SIZE)
        val mphy = Master("mphy", MPHY.BUS_SIZE)
        val punit = Master("punit", PUNIT.BUS_SIZE)
        val rcrb = Master("rcrb", RCRB.BUS_SIZE)

        val smb_mem = Master("smb_mem", SMB_PCU.BUS_SIZE)
        val smb_io = Master("smb_io", SMB_PCU.BUS_SIZE)

        val smb20_0_mem = Master("smb20_0_mem", SMB_20.BUS_SIZE)
        val smb20_1_mem = Master("smb20_1_mem", SMB_20.BUS_SIZE)

        val usb20 = Master("usb20", USB20.BUS_SIZE)

        val sata2 = Master("sata2", SATA.BUS_SIZE)
        val sata3 = Master("sata3", SATA.BUS_SIZE)

        val e1000 = Master("e1000", E1000.BUS_MEM_SIZE)

        

        inner class Ecam() : Master("ecam", PCI_ECAM_BUS_SIZE) {
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

        SATA.BUS_MEM_INDEX_2 to ports.sata2,
        SATA.BUS_MEM_INDEX_3 to ports.sata3,

        E1000.BUS_MEM_INDEX to ports.e1000,
        

        PCI_ECAM_BUS_INDEX to ports.ecam
    )

    private val memory = MappingArea(ports.mem_in,"MEMORY", outputs, BRIDGE_MEM_BUS_INDEX)
    private val io = MappingArea(ports.io_in, "IO", outputs, BRIDGE_IO_BUS_INDEX)

    @DontAutoSerialize
    private val mappings = mapOf(MEMORY_AREA to memory, IO_AREA to io)

    private val mapper = Mapper(ports.mapper, "REMAP", mappings)

    override fun serialize(ctxt: GenericSerializer) = super<IAutoSerializable>.serialize(ctxt)

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) =
        super<IAutoSerializable>.deserialize(ctxt, snapshot)

    override fun stringify() = mappings.values.joinToString("\n") { it.stringify() }
}