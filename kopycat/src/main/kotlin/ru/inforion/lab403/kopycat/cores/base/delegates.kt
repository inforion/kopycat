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
package ru.inforion.lab403.kopycat.cores.base

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.interfaces.IValuable
import kotlin.reflect.KProperty


//class rbit<in T: IValuable>(val index: Int, val initial: Long = 0) {
//    operator fun getValue(thisRef: T, property: KProperty<*>): Int = thisRef.data[index].toInt()
//    operator fun setValue(thisRef: T, property: KProperty<*>, value: Int) = Unit
//}

//class wbit<in T: IValuable>(val index: Int, val initial: Long = 0) {
//    operator fun getValue(thisRef: T, property: KProperty<*>): Int = 0
//    operator fun setValue(thisRef: T, property: KProperty<*>, value: Int) {
//        thisRef.data = thisRef.data.insert(value, index)
//    }
//}

class bit<in T: IValuable>(val index: Int, val initial: Int = 0) {
    operator fun getValue(thisRef: T, property: KProperty<*>): Int = thisRef.data[index].asInt
    operator fun setValue(thisRef: T, property: KProperty<*>, value: Int) {
        thisRef.data = thisRef.data.insert(value, index)
    }
}

class BitsArray(val item: IValuable, vararg val indexes: Int) {
    operator fun get(index: Int): Int {
        val bitno = indexes[index]
        return item.data[bitno].asInt
    }

    operator fun set(index: Int, value: Int) {
        val bitno = indexes[index]
        item.data = item.data.insert(value, bitno)
    }
}

class bits<in T: IValuable>(vararg val indexes: Int) {
    operator fun getValue(thisRef: T, property: KProperty<*>) = BitsArray(thisRef, *indexes)
}


typealias rbit<T> = bit<T>
typealias wbit<T> = bit<T>
typealias rwbit<T> = bit<T>

// FIXME: use initial
//class rfield<in T: IValuable>(val range: IntRange, val initial: Long = 0) {
//    operator fun getValue(thisRef: T, property: KProperty<*>): Long = thisRef.data[range]
////    operator fun setValue(thisRef: T, property: KProperty<*>, value: Long) = Unit
//}
//
//class wfield<in T: IValuable>(val range: IntRange, val initial: Long = 0) {
//    operator fun getValue(thisRef: T, property: KProperty<*>): Long = 0
//    operator fun setValue(thisRef: T, property: KProperty<*>, value: Long) {
//        thisRef.data = thisRef.data.insert(value, range)
//    }
//}
//
//class reserved<in T: IValuable>(val range: IntRange, val initial: Long = 0) {
//    operator fun getValue(thisRef: T, property: KProperty<*>): Long = 0
////    operator fun setValue(thisRef: T, property: KProperty<*>, value: Long) = Unit
//}

class field<in T: IValuable>(val range: IntRange, val initial: Int = 0) {
    operator fun getValue(thisRef: T, property: KProperty<*>): Int = thisRef.data[range].asInt
    operator fun setValue(thisRef: T, property: KProperty<*>, value: Int) {
        thisRef.data = thisRef.data.insert(value.asULong, range)
    }
}

typealias wfield<T> = field<T>
typealias rfield<T> = field<T>
typealias reserved<T> = field<T>
typealias rwfield<T> = field<T>