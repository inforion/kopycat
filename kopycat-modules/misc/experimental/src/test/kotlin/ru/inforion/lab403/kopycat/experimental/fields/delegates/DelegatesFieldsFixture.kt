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
package ru.inforion.lab403.kopycat.experimental.fields.delegates

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.experimental.fields.common.*
import ru.inforion.lab403.kopycat.experimental.fields.interfaces.IMemoryRef
import ru.inforion.lab403.kopycat.experimental.fields.interfaces.IOffsetable
import ru.inforion.lab403.kopycat.modules.BUS32


class DelegatesFieldsFixture(parent: Module, name: String) : Module(parent, name) {
    val dmam = ports.Master("dmam", BUS32)

    inner class AbsoluteData : IMemoryRef {
        // For absolute fields delegates
        override val memory = dmam;

        var field10 by absoluteField("FIELD_10", 0x10uL, 4)
        val field20 by absoluteField("FIELD_20", 0x20uL, 4)
    }

    val absoluteData = AbsoluteData()

    inner class OffsetData : IOffsetable {
        override val memory = dmam;
        override val baseAddress: ULong = 0x50uL;

        var offset10 by offsetField("FIELD_O_10", 0x10uL, 0x8)
        val offset20 by offsetField("FIELD_O_20", 0x20uL, 0x8)

        // Absolute address 0x80
        var dynamicBaseAddress by offsetField("FIELD_D_BASE_ADDRESS", 0x30u, 0x8)
    }

    val offsetData = OffsetData()

    inner class DynamicAbsoluteData : IMemoryRef {
        override val memory = dmam;

        // Address depends on fieldBaseAddress
        // `*((*fieldBaseAddress) * 2 + 0xC)`
        var fieldA10 by dynamicAbsoluteField("FIELD_A_10", 4)
        { offsetData.dynamicBaseAddress * 2u + 0xCu }

        val fieldA20 by dynamicAbsoluteField("FIELD_A_20", 4)
        { offsetData.dynamicBaseAddress * 2u + 0x8u }
    }

    val dynamicAbsoluteData = DynamicAbsoluteData()

    inner class DynamicOffsetData : IMemoryRef {
        override val memory = dmam;

        inner class dynamicOffsetInnerField<in T : IMemoryRef>(name: String, offset: ULong, size: Int) :
            dynamicOffsetField<T>(name, offset, size, 0, { offsetData.dynamicBaseAddress })

        var fieldDOffset10 by dynamicOffsetInnerField("FIELD_D_10", 0x10uL, 0x8)
        val fieldDOffset20 by dynamicOffsetInnerField("FIELD_D_20", 0x20uL, 0x8)
    }

    val dynamicOffsetData = DynamicOffsetData()
}
