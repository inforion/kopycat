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

data class Config0(override var data: ULong = 0uL) : IValuable {
    /** Denotes that the Config1 register is implemented at a select field value of 1 */
    var M by bit(31)

    /**
     * For processors that implement a Fixed Mapping MMU,
     * this field specifies the kseg2 and kseg3 cacheability and
     * coherency attribute
     */
    var K23 by field(30..28)

    /**
     * For processors that implement a Fixed Mapping MMU,
     * this field specifies the kuseg cacheability and
     * coherency attribute
     */
    var KU by field(27..25)

    /** This field is reserved for implementations */
    var Impl by field(24..16)

    /**
     * Indicates the endian mode in which the processor is running
     *
     * |Encoding|Meaning|
     * |-|-|
     * |0|Little endian|
     * |1|Big endian|
     */
    var BE by bit(15)

    /** Architecture Type implemented by the processor */
    var AT by field(14..13)

    /** MIPS64 Architecture revision level */
    var AR by field(12..10)

    /** MMU Type */
    var MT by field(9..7)

    /** Virtual instruction cache */
    var VI by bit(3)

    /** Kseg0 cacheability and coherency attribute */
    var K0 by field(2..0)
}
