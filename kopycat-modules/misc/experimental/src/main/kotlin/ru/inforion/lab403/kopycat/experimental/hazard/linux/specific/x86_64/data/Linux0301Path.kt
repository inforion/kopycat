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
package ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.data

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.auxiliary.fields.common.OffsetData
import ru.inforion.lab403.kopycat.auxiliary.fields.common.OffsetField
import ru.inforion.lab403.kopycat.auxiliary.fields.interfaces.IOffsetable
import ru.inforion.lab403.kopycat.interfaces.IReadWrite


/**
 * [Sources on bootlin](https://elixir.bootlin.com/linux/v3.1/source/include/linux/path.h#L7)
 */
class Linux0301Path(offsetable: IOffsetable) {
    constructor(memory: IReadWrite, baseAddress: ULong) :
            this(OffsetData(memory, baseAddress))

    private val _offsetable = offsetable

    companion object {
        const val SIZEOF = 0x10uL;
    }

    inner class Raw {
        val mnt = OffsetField(_offsetable, "mnt", 0x0uL, Datatype.QWORD)
        val dEntry = OffsetField(_offsetable, "dentry", 0x8uL, Datatype.QWORD)
    }

    val _raw = Raw()

    val dEntry get() = Linux0301DEntry(_offsetable.memory, _raw.dEntry.data)
}
