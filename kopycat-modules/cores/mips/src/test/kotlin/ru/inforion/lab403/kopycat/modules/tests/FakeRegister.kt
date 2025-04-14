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
package ru.inforion.lab403.kopycat.modules.tests

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype

class FakeRegister(parent: Module, name: String, value: ULong): Module(parent, name) {
    companion object {
        @Transient val log = logger(INFO)
    }

    inner class Ports : ModulePorts(this) {
        val mem = Port("mem")
    }

    override val ports = Ports()

    val register = object : Register(ports.mem, 0u, Datatype.DWORD, name) {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            log.warning { "Write from $name at address ${ea.hex8}" }
        }

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            log.warning { "Read from $name at address ${ea.hex8}" }
            return value
        }
    }
}
