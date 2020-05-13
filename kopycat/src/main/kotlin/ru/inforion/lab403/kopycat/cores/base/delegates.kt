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