/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
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
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Controls.VOID
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Type.MEM

/**
 * {RU}
 * Класс реализующий доступ к физическим адресам памяти
 *
 *
 * @param T шаблон типа AGenericCore
 *
 * @property dtype тип данных
 * @property atyp тип указателя
 * @property addr адрес в памяти
 * @property access параметр доступа
 * {RU}
 */
open class Memory<in T: AGenericCore>(
        dtyp: Datatype,
        val atyp: Datatype, // pointer type
        val addr: Long,
        access: Access,
        num: Int = WRONGI) :
        AOperand<T>(MEM, access, VOID, num, dtyp) {

    final override fun effectiveAddress(core: T): Long = addr like atyp

    override fun value(core: T, data: Long) = core.write(dtyp, effectiveAddress(core), data)
    override fun value(core: T): Long = core.read(dtyp, effectiveAddress(core))

    override fun equals(other: Any?): Boolean =
            other is Memory<*> &&
                    other.type == MEM &&
                    other.dtyp == dtyp &&
                    other.addr == addr &&
                    other.specflags == specflags

    override fun hashCode(): Int {
        var result = type.hashCode()
//        result += 31 * result + dtyp
        result += 31 * result + addr.hashCode()
        result += 31 * result + specflags.hashCode()
        return result
    }

    override fun toString(): String = "${dtyp}_%08X".format(addr)
}