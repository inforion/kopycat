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
        val log = logger()
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