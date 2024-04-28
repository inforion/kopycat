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

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import java.util.logging.Level.CONFIG

class HPET(parent: Module, name: String) : Module(parent, name) {

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", 0x400)
    }

    override val ports = Ports()

//    FED0_0000 - FED0_03FF

    private val HPET_GCID = Register(ports.mem, 0x00u, DWORD, "HPET_GCID", 0u, level = CONFIG)
    private val HPET_GCFG = Register(ports.mem, 0x10u, DWORD, "HPET_GCFG", 0u, level = CONFIG)
    private val HPET_GIS = Register(ports.mem, 0x20u, DWORD, "HPET_GIS", 0u, level = CONFIG)

    private val HPET_MCV = object : Register(ports.mem, 0xF0u, DWORD, "HPET_MCV", 0u, level = CONFIG) {
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            data = core.clock.totalTicks
            return super.read(ea, ss, size)
        }
    }

    private val HPET_T0C_L = Register(ports.mem, 0x100u, DWORD, "HPET_T0C_L", 0x0000_0030u, level = CONFIG)
    private val HPET_T0C_H = Register(ports.mem, 0x104u, DWORD, "HPET_T0C_H", 0x00F0_0000u, level = CONFIG)

    private val HPET_T1C_L = Register(ports.mem, 0x120u, DWORD, "HPET_T1C_L", 0x0000_0000u, level = CONFIG)
    private val HPET_T1C_H = Register(ports.mem, 0x124u, DWORD, "HPET_T1C_H", 0x00F0_0000u, level = CONFIG)

    private val HPET_T2C_L = Register(ports.mem, 0x140u, DWORD, "HPET_T2C_L", 0x0000_0000u, level = CONFIG)
    private val HPET_T2C_H = Register(ports.mem, 0x144u, DWORD, "HPET_T2C_H", 0x00F0_0800u, level = CONFIG)


    private val HPET_T0CV_L = Register(ports.mem, 0x108u, DWORD, "HPET_T0CV_L", 0xFFFF_FFFFu, level = CONFIG)
    private val HPET_T0CV_U = Register(ports.mem, 0x10Cu, DWORD, "HPET_T0CV_U", 0xFFFF_FFFFu, level = CONFIG)

    private val HPET_T1CV = Register(ports.mem, 0x128u, DWORD, "HPET_T1CV", 0xFFFF_FFFFu, level = CONFIG)
    private val HPET_T2CV = Register(ports.mem, 0x148u, DWORD, "HPET_T2CV", 0xFFFF_FFFFu, level = CONFIG)
}