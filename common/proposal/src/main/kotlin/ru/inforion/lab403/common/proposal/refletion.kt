package ru.inforion.lab403.common.proposal

import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaType

/**
 * {EN}
 * Implements 'reverse' [Class.isAssignableFrom] for type of [KProperty1] field
 *
 * @param cls basic Java class
 *
 *
 * @return true if type of field is subtype of specified class [cls]
 * {EN}
 */
infix fun <T, R>KProperty1<T, R>.isTypeSubtypeOf(cls: Class<*>): Boolean {
    val type = returnType.javaType as? Class<*> ?: return false
    return cls.isAssignableFrom(type)
}