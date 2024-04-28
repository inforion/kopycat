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
package ru.inforion.lab403.kopycat.modules.atom2758.sata

import ru.inforion.lab403.kopycat.cores.base.MasterPort
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.auxiliary.fields.common.OffsetField
import ru.inforion.lab403.kopycat.auxiliary.fields.delegates.offsetField
import ru.inforion.lab403.kopycat.auxiliary.fields.interfaces.IMemoryRef
import ru.inforion.lab403.kopycat.auxiliary.fields.interfaces.IOffsetable

/**
 * Таблица с командой.
 *
 * AHCI 1.0 specification, страница 29
 *
 * @param memory DMA порт
 * @param baseAddress базовый адрес таблицы
 */
internal class CommandTable(override val memory: MasterPort, override val baseAddress: ULong) : IMemoryRef,
    IOffsetable {
    // Command FIS 0 - 40h

    /** FIS_REGISTER_H2D */
    var CFIS_TYPE by offsetField("CFIS_TYPE", 0uL, Datatype.BYTE)

    inner class CFIS_PMP_CLASS(addr: ULong) : OffsetField(this, "CFIS_PMP", addr, Datatype.BYTE) {
        var pmport by field(3..0)
        // var rsv by field(6..4)
        var c by bit(7)
    }

    var CFIS_PMP = CFIS_PMP_CLASS(1uL)
    var CFIS_CMD by offsetField("CFIS_CMD", 2uL, Datatype.BYTE)
    var CFIS_FEATL by offsetField("CFIS_FEATL", 3uL, Datatype.BYTE)

    var CFIS_LBA0 by offsetField("CFIS_LBA0", 4uL, Datatype.BYTE)
    var CFIS_LBA1 by offsetField("CFIS_LBA1", 5uL, Datatype.BYTE)
    var CFIS_LBA2 by offsetField("CFIS_LBA2", 6uL, Datatype.BYTE)
    var CFIS_DEV by offsetField("CFIS_DEV", 7uL, Datatype.BYTE)

    var CFIS_LBA3 by offsetField("CFIS_LBA3", 8uL, Datatype.BYTE)
    var CFIS_LBA4 by offsetField("CFIS_LBA4", 9uL, Datatype.BYTE)
    var CFIS_LBA5 by offsetField("CFIS_LBA5", 10uL, Datatype.BYTE)
    // var CFIS_FEATH by offsetField("CFIS_FEATH", 11uL, Datatype.BYTE)

    var CFIS_COUNTL by offsetField("CFIS_COUNTL", 12uL, Datatype.BYTE)
    var CFIS_COUNTH by offsetField("CFIS_COUNTH", 13uL, Datatype.BYTE)
    // var CFIS_ICC by offsetField("CFIS_ICC", 14uL, Datatype.BYTE)
    var CFIS_CONTROL by offsetField("CFIS_CONTROL", 15uL, Datatype.BYTE)

    // ATAPI command 0x40 - 0x60
}
