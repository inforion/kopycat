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
package ru.inforion.lab403.kopycat.cores.mips

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.FPR
import kotlin.reflect.KProperty


class LongOperandField(val index: Int) {
    operator fun getValue(thisRef: AMipsInstruction, property: KProperty<*>): Long = thisRef[index - 1].value(thisRef.core)
    operator fun setValue(thisRef: AMipsInstruction, property: KProperty<*>, value: Long) {
        thisRef[index - 1].value(thisRef.core, value)
    }
}

class IntOperandField(val index: Int) {
    operator fun getValue(thisRef: AMipsInstruction, property: KProperty<*>): Int = thisRef[index - 1].value(thisRef.core).toInt()
    operator fun setValue(thisRef: AMipsInstruction, property: KProperty<*>, value: Int) {
        thisRef[index - 1].value(thisRef.core, value.toULong())
    }
}

class DoubleRegister(val index: Int) {
    operator fun getValue(thisRef: AMipsInstruction, property: KProperty<*>): Long {
        val op1 = thisRef[index - 1] as FPR
        val op2 = FPR(op1.reg + 1)
//        TODO("CHECK IT")
        return op1.value(thisRef.core).insert(op2.value(thisRef.core), 63..32)
    }
    operator fun setValue(thisRef: AMipsInstruction, property: KProperty<*>, value: Long) {
        val op1 = thisRef[index - 1] as FPR
        val op2 = FPR(op1.reg + 1)
        op1.value(thisRef.core, value[31..0])
        op2.value(thisRef.core, value[63..32])
//        TODO("CHECK IT")
    }
}