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
package ru.inforion.lab403.kopycat.experimental.fields

import ru.inforion.lab403.common.extensions.hex2
import ru.inforion.lab403.common.extensions.unhexlify
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.experimental.fields.common.CommonFieldsFixture
import ru.inforion.lab403.kopycat.experimental.fields.delegates.DelegatesFieldsFixture
import ru.inforion.lab403.kopycat.modules.memory.RAM

class FieldsTopFixture : Module(null, "Core Test Fixture") {
    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem")
    }

    override val buses = Buses()

    private val preparedData = (0x00..0xFF)
        .joinToString("") { it.hex2 }
        .repeat(4)
        .unhexlify()

    val ramDynamic = RAM(this, "ram_dyna", 0x10000)
    val ramPrepared = RAM(this, "ram_prepared", preparedData.size, preparedData)

    val fields = CommonFieldsFixture(this, "FIELDS_COMMON")
    val delegateFields = DelegatesFieldsFixture(this, "FIELDS_DELEGATE")

    init {
        ramDynamic.ports.mem.connect(buses.mem, 0x1000_0000uL)
        ramPrepared.ports.mem.connect(buses.mem, 0x0uL)
        fields.dmam.connect(buses.mem, 0x0uL)
        delegateFields.dmam.connect(buses.mem, 0x0uL)

        initializePortsAndBuses()
    }
}
