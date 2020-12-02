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
package ru.inforion.lab403.kopycat.auxiliary

import ru.inforion.lab403.common.extensions.getInt
import ru.inforion.lab403.common.extensions.putInt
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import java.nio.ByteOrder
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties


open class Blob(val size: Int, val order: ByteOrder) {
    companion object {
        @Transient val log = logger()
    }

    private val data = ByteArray(size)

    protected inner class Field<in T: Blob>(val offset: Int, val dtyp: Datatype, var value: Long) :
            ReadWriteProperty<T, Long> {

        private var initialized = false

        override fun getValue(thisRef: T, property: KProperty<*>): Long {
            if (!initialized) {
                put(offset, dtyp, value)
                initialized = true
            }
            return get(offset, dtyp)
        }

        override operator fun setValue(thisRef: T, property: KProperty<*>, value: Long) = put(offset, dtyp, value)
    }

    protected fun <T: Blob>byte(offset: Int, value: Long = 0) = Field<T>(offset, Datatype.BYTE, value)
    protected fun <T: Blob>word(offset: Int, value: Long = 0) = Field<T>(offset, Datatype.WORD, value)
    protected fun <T: Blob>tribyte(offset: Int, value: Long = 0) = Field<T>(offset, Datatype.TRIBYTE, value)
    protected fun <T: Blob>dword(offset: Int, value: Long = 0) = Field<T>(offset, Datatype.DWORD, value)
    protected fun <T: Blob>qword(offset: Int, value: Long = 0) = Field<T>(offset, Datatype.QWORD, value)

    fun get(offset: Int, dtyp: Datatype): Long = get(offset, dtyp.bytes)
    fun put(offset: Int, dtyp: Datatype, value: Long) = put(offset, dtyp.bytes, value)

    fun get(offset: Int, size: Int): Long = data.getInt(offset, size, order)
    fun put(offset: Int, size: Int, value: Long) = data.putInt(offset, value, size, order)

    fun init() {
        // spooling delegate properties initial values
        javaClass.kotlin.declaredMemberProperties.forEach {
            val value = it.get(this)
            log.config { "${it.name} = %08X".format(value) }
        }
        // log.config { "Blob = ${data.hexlify()}" }
    }
}