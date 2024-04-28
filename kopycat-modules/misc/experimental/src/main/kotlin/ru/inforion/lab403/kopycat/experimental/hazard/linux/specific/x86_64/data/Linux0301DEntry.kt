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
import ru.inforion.lab403.kopycat.experimental.runtime.DataUtils
import ru.inforion.lab403.kopycat.interfaces.IReadWrite
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div

class Linux0301DEntry(offsetable: IOffsetable) {
    constructor(memory: IReadWrite, baseAddress: ULong) :
            this(OffsetData(memory, baseAddress))

    private val _offsetable = offsetable
    private val _data = DataUtils(offsetable.memory)

    inner class Raw {
        val dFlags = OffsetField(_offsetable, "d_flags", 0x00uL, Datatype.DWORD)
        val dSeq = OffsetField(_offsetable, "d_seq", 0x04uL, Datatype.DWORD)
        val dHash = OffsetField(_offsetable, "d_hash", 0x08uL, Datatype.QWORD)
        val dParent = OffsetField(_offsetable, "d_parent", 0x18uL, Datatype.QWORD)
        val dNameHashLen = OffsetField(_offsetable, "d_name.hash_len", 0x20uL, Datatype.QWORD)
        val dNameName = OffsetField(_offsetable, "d_name.name", 0x28uL, Datatype.QWORD)
        val dINode = OffsetField(_offsetable, "f_inode", 0x30uL, Datatype.QWORD)
        val dIName = OffsetField(_offsetable, "d_iname", 0x38uL, Datatype.QWORD)
    }

    val _raw = Raw()

    val hashListNode get() = Linux0301HListBLNode(_offsetable.memory, _raw.dHash.address)
    val parent get() = Linux0301DEntry(_offsetable.memory, _raw.dParent.data)
    val name get() = _data.readStringN(_raw.dNameName.data, 0x0, 4096)
    val shortName get() = _data.readStringN(_raw.dIName.address, 0x0, 32)

    val fullPath: Path
        get() {
            val MAX_ITERATIONS = 1024
            var iterations = 0

            var path = Path("")
            var dentryIter = this@Linux0301DEntry
            while (true) {
                path = Path(dentryIter.name) / path

                if (dentryIter.parent._offsetable.baseAddress == 0x0uL || dentryIter.parent._offsetable == dentryIter._offsetable) {
                    break
                }
                dentryIter = dentryIter.parent

                iterations++
                if (iterations > MAX_ITERATIONS) {
                    throw IllegalStateException("fullPath reached MAX_ITERATIONS limit. Path: '${this}'")

                }
            }

            return path
        }
}
