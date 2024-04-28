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
package ru.inforion.lab403.kopycat.experimental.fields.common

import ru.inforion.lab403.kopycat.auxiliary.fields.common.*
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.modules.BUS32


class CommonFieldsFixture(parent: Module, name: String) : Module(parent, name) {
    val dmam = ports.Master("dmam", BUS32)

    val field10 = AbsoluteField(dmam, "FIELD_10", 0x10uL, 4)
    val field20 = AbsoluteReadField(dmam, "FIELD_20", 0x20uL, 4)

    val offsetable = OffsetData(dmam, 0x50uL);
    val fieldOffset10 = OffsetField(offsetable, "FIELD_O_10", 0x10uL, 0x8)
    val fieldOffset20 = OffsetReadField(offsetable, "FIELD_O_20", 0x20uL, 0x8)

    // Absolute address 0x80
    val fieldBaseAddress = OffsetField(offsetable, "FIELD_D_BASE_ADDRESS", 0x30u, 0x8)

    // Address depends on fieldBaseAddress
    // `*((*fieldBaseAddress) * 2 + 0xC)`
    val fieldA10 = DynamicAbsoluteField(dmam, "FIELD_A_10", 4) { fieldBaseAddress.data * 2u + 0xCu }

    val fieldA20 = DynamicAbsoluteReadField(dmam, "FIELD_A_20", 4) { fieldBaseAddress.data * 2u + 0x8u }

    inner class DynamicOffsetInnerField(name: String, offset: ULong, size: Int) :
        DynamicOffsetField(dmam, name, offset, size, 0, { fieldBaseAddress.data })

    inner class DynamicOffsetInnerReadField(name: String, offset: ULong, size: Int) :
        DynamicOffsetReadField(dmam, name, offset, size, 0, { fieldBaseAddress.data })

    val fieldDOffset10 = DynamicOffsetInnerField("FIELD_D_10", 0x10uL, 0x8)
    val fieldDOffset20 = DynamicOffsetInnerReadField("FIELD_D_20", 0x20uL, 0x8)
}
