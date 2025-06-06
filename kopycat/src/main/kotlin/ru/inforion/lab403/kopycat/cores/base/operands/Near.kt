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
package ru.inforion.lab403.kopycat.cores.base.operands

import ru.inforion.lab403.common.extensions.WRONGI
import ru.inforion.lab403.common.extensions.ulong_s
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.READ
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Controls.VOID
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Type.NEAR

open class Near<in T: AGenericCore>(val offset: Int, dtyp: Datatype, num: Int = WRONGI) :
        AOperand<T>(NEAR, READ, VOID, num, dtyp) {

    override fun value(core: T): ULong = offset.ulong_s
    override fun toString(): String = "%08X".format(offset)

    final override fun value(core: T, data: ULong): Unit = throw UnsupportedOperationException("Can't write to near value")

    override fun equals(other: Any?): Boolean =
            other is Near<*> &&
            other.type == NEAR &&
            other.offset == offset &&
            other.specflags == specflags

    override fun hashCode(): Int {
        var result = type.hashCode()
        result += 31 * result + offset.hashCode()
        result += 31 * result + specflags.hashCode()
        return result
    }
}