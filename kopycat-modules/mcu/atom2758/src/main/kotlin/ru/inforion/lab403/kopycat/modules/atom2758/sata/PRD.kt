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

import ru.inforion.lab403.kopycat.cores.base.MasterPort
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.auxiliary.fields.delegates.offsetField
import ru.inforion.lab403.kopycat.auxiliary.fields.interfaces.IOffsetable

/**
 * Запись в таблице PRDT.
 *
 * AHCI 1.0 specification, страница 29
 */
internal class PRD(override val memory: MasterPort, override val baseAddress: ULong) : IOffsetable {
    val ADDR by offsetField("ADDR", 0uL, Datatype.QWORD)
    private val DBC by offsetField("DBC", 12uL, Datatype.DWORD)

    /** Размер области памяти */
    val entrySize by lazy { (DBC and 0x3fffffuL) + 1uL }

    /** Возвращает следующую запись */
    fun next() = PRD(memory, baseAddress + 16uL)
}
