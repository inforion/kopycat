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
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Type.REG

/**
 * {RU}
 * Абстрактный класс, который описывает базовую функциональность регистра CPU (и сопроцессора)
 *
 * Примечание: Регистр не может быть открыт из-за ограничений процессора
 * {RU}
 *
 * {EN}
 * Abstract class that describe basic functionality of CPU register (coproc. also) e.g. mov eax, edx
 *
 * NOTE: Register can't be open due to bounds of CPU very nature
 * {EN}
 */
abstract class ARegister<in T: AGenericCore>(
        val reg: Int,
        access: Access,
        dtyp: Datatype = DWORD,
        num: Int = WRONGI) :
        AOperand<T>(REG, access, Controls.VOID, num, dtyp) {

    // NOTE: value(dev: T, ...) methods don't required use 'like dtyp' because it implemented at ARegisterBank level

    override fun equals(other: Any?): Boolean =
            other is ARegister<*> &&
                    other.type == REG &&
                    other.reg == reg &&
                    (other.specflags xor specflags) and Access.ANY.flags.inv() == 0

    override fun hashCode(): Int {
        var result = type.hashCode()
        result += 31 * result + reg.hashCode()
        result += 31 * result + specflags.hashCode()
        return result
    }
}