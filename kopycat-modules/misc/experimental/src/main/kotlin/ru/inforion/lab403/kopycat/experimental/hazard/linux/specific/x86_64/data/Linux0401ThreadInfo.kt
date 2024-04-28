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
import ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.data.interfaces.LinuxThreadInfo
import ru.inforion.lab403.kopycat.interfaces.IReadWrite

/**
 * [Sources on bootlin](https://elixir.bootlin.com/linux/v4.1/source/arch/x86/include/asm/thread_info.h#L52)
 *
 * @see LinuxThreadInfo
 */
class Linux0401ThreadInfo(offsetable: IOffsetable) : LinuxThreadInfo {
    constructor(memory: IReadWrite, baseAddress: ULong) :
            this(OffsetData(memory, baseAddress))

    private val _offsetable = offsetable

    inner class Raw {
        val taskPtr = OffsetField(_offsetable, "task", 0x0uL, Datatype.QWORD)
        val flags = OffsetField(_offsetable, "flags", 0x8uL, Datatype.DWORD)
        val status = OffsetField(_offsetable, "status", 0xCuL, Datatype.DWORD)
        val cpu = OffsetField(_offsetable, "cpu", 0x10uL, Datatype.DWORD)
        val preemptCount = OffsetField(_offsetable, "preempt_count", 0x14uL, Datatype.DWORD)
        val addrLimit = OffsetField(_offsetable, "addr_limit", 0x18uL, Datatype.QWORD)
    }

    val _raw = Raw()

    override var addrLimit: ULong
        get() = _raw.addrLimit.data
        set(value) {
            _raw.addrLimit.data = value
        }

    override fun isAddrInLimit(address: ULong) = addrLimit > address
}
