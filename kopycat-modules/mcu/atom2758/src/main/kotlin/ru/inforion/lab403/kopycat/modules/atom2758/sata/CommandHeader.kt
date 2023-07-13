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
package ru.inforion.lab403.kopycat.modules.atom2758.sata

import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.experimental.fields.common.OffsetField
import ru.inforion.lab403.kopycat.experimental.fields.delegates.offsetField
import ru.inforion.lab403.kopycat.experimental.fields.interfaces.IMemoryRef
import ru.inforion.lab403.kopycat.experimental.fields.interfaces.IOffsetable

/**
 * Заголовок команды.
 *
 * AHCI 1.0 specification, страница 27
 *
 * @param port порт SATA контроллера
 * @param idx индекс в таблице
 */
internal class CommandHeader(private val port: Port, private val idx: ULong) : IMemoryRef, IOffsetable {
    override val memory = port.hba.dmam
    override val baseAddress = port.CLB.data + idx * 32u

    inner class OPTS_CLASS : OffsetField(this, "OPTS", 0uL, Datatype.WORD) {
        var atapi by bit(5)
        var write by bit(6)
        // var prefetch by bit(7)
        // var reset by bit(8)
        // var clrBusy by bit(10)
    }

    var OPTS = OPTS_CLASS()
    private var PRDTL by offsetField("PRDTL", 2uL, Datatype.WORD)
    var PRDBC by offsetField("PRDBC", 4uL, Datatype.DWORD)
    private var CTBA by offsetField("CTBA", 8uL, Datatype.QWORD)

    /** Возвращает таблицу с командой */
    fun table() = CommandTable(port.hba.dmam, CTBA)

    /** Последовательность записей в таблице */
    fun prds() = sequence {
        var current = PRD(port.hba.dmam, CTBA + 0x80uL)

        for (i in 0uL until PRDTL) {
            yield(current)
            current = current.next()
        }
    }
}
