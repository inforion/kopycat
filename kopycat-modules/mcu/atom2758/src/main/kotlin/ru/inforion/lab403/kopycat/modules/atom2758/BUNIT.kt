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

import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.inv
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.BYTE
import ru.inforion.lab403.kopycat.cores.base.extensions.PCI_ECAM_BUS_INDEX
import ru.inforion.lab403.kopycat.cores.base.extensions.mapOffset
import ru.inforion.lab403.kopycat.modules.PCI_ECAM_BUS_SIZE
import ru.inforion.lab403.kopycat.modules.PCI_MEM_AREA
import java.util.logging.Level.CONFIG

class BUNIT(parent: Module, name: String) : Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val msg = Slave("msg", MESSAGE_PORT_SIZE)

        val mapper = Master("mapper")
    }

    override val ports = Ports()

    private val SERVICE = MESSAGE_BUS_SERVICE_REGISTER(ports.msg) { _, ss, _ ->
        when (ss) {
            6, 0x10 -> MESSAGE_BUS_READ_OPERATION
            7, 0x11 -> MESSAGE_BUS_WRITE_OPERATION
            else -> error("Unknown BUNIT opcode: 0x${ss.hex}")
        }
    }

    private val BNOCACHE = Register(ports.msg, 0x23u, BYTE, "BNOCACHE", level = CONFIG)
    private val BNOCACHECTL = Register(ports.msg, 0x24u, BYTE, "BNOCACHECTL", level = CONFIG)
    private val BMBOUND = Register(ports.msg, 0x25u, BYTE, "BMBOUND", 0x40000000u, level = CONFIG)
    private val BMBOUND_HI = Register(ports.msg, 0x26u, BYTE, "BMBOUND_HI", 0x80000000u, level = CONFIG)

    private val BECREG = object : Register(ports.msg, 0x27u, BYTE, "BECREG", level = CONFIG) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            // Each device has its own 4 KB space and each device's info is accessible through a simple array
            // dev[bus][device][function] so that 256 MB of physical contiguous space is "stolen" for this use
            // (256 buses × 32 devices × 8 functions × 4 KB = 256 MB). The base physical address of this array
            // is not specified. For example, on modern x86 systems the ACPI tables contain the necessary
            // information.[8]
            val from = data and inv(1uL)
            ports.mapper.mapOffset(name, from, PCI_ECAM_BUS_SIZE, PCI_MEM_AREA, PCI_ECAM_BUS_INDEX)
        }
    }

    private val BMISC = Register(ports.msg, 0x28u, BYTE, "BMISC", level = CONFIG)

    // https://github.com/coreboot/coreboot/blob/master/src/soc/intel/baytrail/include/soc/iosf.h
    private val BUNIT_SMRCP = Register(ports.msg, 0x2Bu, BYTE, "BUNIT_SMRCP", level = CONFIG)
    private val BUNIT_SMRRAC = Register(ports.msg, 0x2Cu, BYTE, "BUNIT_SMRRAC", level = CONFIG)
    private val BUNIT_SMRWAC = Register(ports.msg, 0x2Du, BYTE, "BUNIT_SMRWAC", level = CONFIG)

    // Intel docs says SMM area region is programmed without clue where and whom, so this is my imagination
    private val BSMMRRL = Register(ports.msg, 0x2Eu, BYTE, "BSMMRRL", 0x06C0u, level = CONFIG)
    private val BSMMRRH = Register(ports.msg, 0x2Fu, BYTE, "BSMMRRH", 0x06C4u, level = CONFIG)

    private val BIMR0L = Register(ports.msg, 0x80u, BYTE, "BIMR0L", level = CONFIG)
    private val BIMR0H = Register(ports.msg, 0x81u, BYTE, "BIMR0H", level = CONFIG)
    private val BIMR0RAC = Register(ports.msg, 0x82u, BYTE, "BIMR0RAC", level = CONFIG)
    private val BIMR0WAC = Register(ports.msg, 0x83u, BYTE, "BIMR0WAC", level = CONFIG)

    // Unknown registers that is being used in runtime
    private val AREA_08_1F = Memory(ports.msg, 0x08u, 0x1Fu, "AREA_08_1F", ACCESS.R_W)
    private val AREA_38_4F = Memory(ports.msg, 0x38u, 0x4Fu, "AREA_38_4F", ACCESS.R_W)
}
