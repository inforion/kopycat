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
package ru.inforion.lab403.kopycat.cores.mips.config

import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.interfaces.IValuable

data class Config1(override var data: ULong = 0uL) : IValuable {
    /** This bit is reserved to indicate that a Config2 register is present */
    var M by bit(31)

    /** Number of entries in the TLB minus one */
    var MMUSize by field(30..25)

    var IS by field(24..22)
    var IL by field(21..19)
    var IA by field(18..16)
    var DS by field(15..13)
    var DL by field(12..10)
    var DA by field(9..7)

    /** Coprocessor 2 implemented */
    var C2 by bit(6)

    /** MDMX ASE implemented */
    var MD by bit(5)

    /** Performance Counter registers implemented */
    var PC by bit(4)

    /** Watch registers implemented */
    var WR by bit(3)

    /** Code compression (MIPS16e) implemented */
    var CA by bit(2)

    /** EJTAG implemented */
    var EP by bit(1)

    /** FPU implemented */
    var FP by bit(0)
}
