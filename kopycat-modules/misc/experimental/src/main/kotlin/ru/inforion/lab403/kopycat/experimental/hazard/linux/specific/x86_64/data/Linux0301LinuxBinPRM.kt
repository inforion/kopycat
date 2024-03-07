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
package ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.data

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.auxiliary.fields.common.OffsetData
import ru.inforion.lab403.kopycat.auxiliary.fields.common.OffsetField
import ru.inforion.lab403.kopycat.auxiliary.fields.interfaces.IOffsetable
import ru.inforion.lab403.kopycat.interfaces.IReadWrite

/**
 * [Sources on bootlin](https://elixir.bootlin.com/linux/v3.1/source/include/linux/binfmts.h#L28)
 *
 * With `CONFIG_MMU`
 */
class Linux0301LinuxBinPRM(offsetable: IOffsetable) {
    constructor(memory: IReadWrite, baseAddress: ULong) :
            this(OffsetData(memory, baseAddress))

    private val _offsetable = offsetable

    companion object {
        const val BINPRM_BUF_SIZE: ULong = 128uL
//        const MAX_ARG_PAGES: Int = 32
    }

    inner class Raw {
        val buf = OffsetField(_offsetable, "buf", 0x00uL, Datatype.QWORD)
        val vma = OffsetField(_offsetable, "vma", BINPRM_BUF_SIZE + 0x00uL, Datatype.QWORD)
        val vmaPages = OffsetField(_offsetable, "vma_pages", BINPRM_BUF_SIZE + 0x08uL, Datatype.QWORD)
        val mm = OffsetField(_offsetable, "mm", BINPRM_BUF_SIZE + 0x10uL, Datatype.QWORD)
        val p = OffsetField(_offsetable, "p", BINPRM_BUF_SIZE + 0x18uL, Datatype.QWORD)
        val bitFlags = OffsetField(_offsetable, "cred_prepared__cap_effective__taso", BINPRM_BUF_SIZE + 0x20uL, Datatype.DWORD)
        val recursionDepth = OffsetField(_offsetable, "recursion_depth", BINPRM_BUF_SIZE + 0x24uL, Datatype.DWORD)
        val file = OffsetField(_offsetable, "file", BINPRM_BUF_SIZE + 0x28uL, Datatype.QWORD)
    }

    val _raw = Raw()

    val file get() = Linux0301File(_offsetable.memory, _raw.file.data)
    val fullPath get() = file.fullPath
}
