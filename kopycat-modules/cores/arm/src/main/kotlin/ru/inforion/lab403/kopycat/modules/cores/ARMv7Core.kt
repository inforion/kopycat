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
package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.ARMv7COP
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.ARMv7CPU
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts



class ARMv7Core constructor(parent: Module, name: String, frequency: Long, ipc: Double):
        AARMCore(parent, name, frequency, 7, ipc) {

    inner class Buses: ModuleBuses(this) {
        val mem = Bus("mem")
    }

    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem")
    }

    override val ports = Ports()
    override val buses = Buses()

    enum class Endianess(val code: Int) {
        BIG_ENDIAN(1),
        LITTLE_ENDIAN(0);
        companion object {
            fun from(code: Int): Endianess = values().first { it.code == code }
        }
    }

    override val cpu = ARMv7CPU(this, "cpu")
    override val cop = ARMv7COP(this, "cop")

    init {
        buses.connect(cpu.ports.mem, ports.mem)
    }
}