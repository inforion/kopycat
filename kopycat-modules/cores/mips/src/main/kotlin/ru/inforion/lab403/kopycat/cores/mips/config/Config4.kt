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
package ru.inforion.lab403.kopycat.cores.mips.config

import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.interfaces.IValuable

data class Config4(override var data: ULong = 0uL) : IValuable {
    /** This bit is reserved to indicate that a Config5 register is present */
    var M by bit(31)

    /** TLB invalidate instruction support/configuration */
    var IE by field(30..29)

    /** If this bit is set, then EntryHI_ASID is extended to 10 bits */
    var AE by bit(28)

    /**
     * Applicable only if ConfigMT = 1 or 4; otherwise, reserved
     *
     * Pre-Release 6: If [MMUExtDef] = 3, this field is concatenated
     * to the left of the most-significant bit of the
     * [Config1.MMUSize] field to indicate the size of the VTLB.
     *
     * Release 6: This field is always concatenated to the left of
     * the most-significant bit of the [Config1.MMUSize].
     */
    var VTLBSizeExt by field(27..24)

    /**
     * Indicates how many scratch registers are available to kernel-mode
     * software within COP0 Register 31
     */
    var KScrExists by field(23..16)

    /** MMU Extension Definition. Defines how Config4[13:0] is to be interpreted. */
    var MMUExtDef by field(15..14)

    /**
     * If [MMUExtDef] = 1 then this field is an extension of [Config1.MMUSize] field.
     * This field is concatenated to the left of the most-significant
     * bit to the [Config1.MMUSize] field to indicate the size of the TLB-1.
     */
    var MMUExt1_MMUSizeExt by field(7..0)
}
