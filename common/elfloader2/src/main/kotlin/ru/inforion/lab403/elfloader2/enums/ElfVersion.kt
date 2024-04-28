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
package ru.inforion.lab403.elfloader2.enums

import ru.inforion.lab403.common.extensions.ubyte


enum class ElfVersion(val id: UByte) {
    EV_NONE(0u),
    EV_CURRENT(1u);

    companion object {
        fun cast(id: UByte, onFail: (UByte) -> ElfVersion) = values().find { it.id == id } ?: onFail(id)
        fun cast(id: UInt, onFail: (UInt) -> ElfVersion) = if (id > 0xFFu)
            onFail(id)
        else
            values().find { it.id == id.ubyte } ?: onFail(id)
    }
}