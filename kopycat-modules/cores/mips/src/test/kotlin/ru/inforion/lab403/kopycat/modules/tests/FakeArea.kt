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
package ru.inforion.lab403.kopycat.modules.tests

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import java.util.logging.Level

class FakeArea(parent: Module, name: String, val size: Long, value: Long): Module(parent, name) {
    companion object {
        val log = logger(Level.INFO)
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", this@FakeArea.size)
    }

    override val ports = Ports()

    val area = object : Area(ports.mem, 0, size - 3, name) {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            log.warning { "Write from $name at address ${ea.hex8}" }
        }

        override fun read(ea: Long, ss: Int, size: Int): Long {
            log.warning { "Read from $name at address ${ea.hex8}" }
            return value
        }

        override fun fetch(ea: Long, ss: Int, size: Int): Long {
            log.warning { "Fetch from $name at address ${ea.hex8}" }
            return value
        }
    }
}