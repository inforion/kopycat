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
package ru.inforion.lab403.kopycat.cores.base.common

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.Port
import ru.inforion.lab403.kopycat.cores.base.enums.AccessType
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.interfaces.IMemoryStream
import java.nio.ByteBuffer
import java.nio.ByteOrder.LITTLE_ENDIAN

/**
 * {RU}
 * Кэшированная поточная работа с памятью.
 * На данный момент поддерживается только поточное чтение.
 * Данный класс призван ускорить работу с много-байтовыми инструкциями.
 *
 *
 * @property reader мастер-порт, который будет производить доступ к памяти
 * @param where адрес на шине для доступа к данным
 * @property ss номер сегмента памяти
 * @property type тип доступа (instruction or data)
 * @property cacheSize размер кэша в байтах (по умолчанию, 16)
 *
 * @property cache кэш-массив байт
 * @property loadedBytes количество прочитанных байтов
 * @property position текущая позиция в кэш-массиве
 * @property mark метка-адрес на шине для доступа к данным
 * @property last последнее прочитанное 32-битное значение
 * @property offset смещение относительно базового адреса на шине данных
 * @property data массив байт, который был вычитан
 * {RU}
 *
 * {EN}
 * Cached stream for memory reading.
 * At current version support only memory reading. This class is created to speed up reading.
 *
 * @property reader master port for memory access
 * @param where address on memory bus
 * @property ss segment selector value
 * @property type access type (instruction or data)
 * @property cacheSize размер кэша в байтах (по умолчанию, 16)
 *
 * @property cache cached byte array
 * @property loadedBytes number of read bytes
 * @property position current position in cached byte array
 * @property mark address mark on bus for data access
 * @property last last read 32bit value
 * @property offset base address offset at bus
 * @property data byte array has been read already
 * {EN}
 */
class CachedMemoryStream(
    private val reader: Port,
    where: ULong,
    val ss: Int,
    val type: AccessType,
    private val cacheSize: Int = 16
) : IMemoryStream {

    companion object {
        const val PAGE_SIZE = 4096
        const val PAGE_OFFSET_MASK: ULong = 0xFFFuL
        const val LOAD_CHUNK_SIZE = 8  // may not be bigger 8
    }

    private var cache = ByteBuffer
        .allocateDirect(cacheSize)
        .apply { order(LITTLE_ENDIAN) }

    private var readBytes = 0  // read from stream bytes
    private var loadedBytes = 0  // loaded to stream bytes

    override var position
        get() = readBytes.ulong_z;
        set(value) {
            readBytes = value.int
        }

    override var mark: ULong = where
    override var last: Int = 0

    private fun workingCacheEA(): ULong = mark + loadedBytes.uint

    private fun fetchCache(size: Int) {
        val ea = workingCacheEA()
        val d = reader.fetch(ea, ss, size)
        writeCache(d, size)
    }

    private fun writeCache(value: ULong, size: Int) {
        cache.putNumber(loadedBytes, value, size)
        loadedBytes += size
    }

    private fun peekCache(size: Int) = cache.getNumber(readBytes, size)

    /**
     * {RU}
     * Внутренний метод чтения значения из кэша
     *
     * @param size количество байт для вычитывания из кэша
     * @return вычитанное значение
     * @throws GeneralException
     * {RU}
     *
     * {EN}
     * Private methods to read from cache
     *
     * @param size bytes count to read from cache
     * @return read value
     * @throws GeneralException
     * {EN}
     */

    fun readCache(size: Int): ULong {
        val data = peekCache(size)
        readBytes += size
        return data
    }

    /**
     * {RU}
     * Сброс объекта-потока
     *
     * @param where адрес на шине данных, к которому будет "сброшен" поток
     * {RU}
     *
     * {EN}
     * Reset stream
     *
     * @param where address in bus to reset stream
     * {EN}
     */
    fun reset(where: ULong) {
        readBytes = 0
        loadedBytes = 0
        mark = where
        load()
    }

    /**
     * {EN}Load data it cache{EN}
     *
     * {RU}Загрузка блока данных в кэш{RU}
     */
    private fun load() {
        val ea = mark + loadedBytes.uint

        val pageOffset = (ea and PAGE_OFFSET_MASK).int
        val left = PAGE_SIZE - pageOffset

        if (left >= LOAD_CHUNK_SIZE) {
            fetchCache(LOAD_CHUNK_SIZE)
        } else {
            fetchCache(left)
            fetchCache(LOAD_CHUNK_SIZE - left)
        }
    }

    /**
     * {RU}
     * Чтение значения из кэша с изменением текущей позиции
     *
     * @param datatype тип данных для загрузки
     * @return вычитанное значение
     * @throws IndexOutOfBoundsException
     * {RU}
     *
     * {EN}
     * Read value from cache with changing read bytes count
     *
     * @param datatype data type to read
     * @return read value
     * @throws IndexOutOfBoundsException
     * {EN}
     */
    override fun read(datatype: Datatype): ULong {
        if (readBytes + datatype.bytes < cacheSize) {
            if (readBytes + datatype.bytes >= loadedBytes)
                load()
            val result = readCache(datatype.bytes)
            last = result.int
            return result
        }
        throw IndexOutOfBoundsException("Cache size is $cacheSize. Attempt to read byte at ${readBytes + datatype.bytes}")
    }

    /**
     * {RU}
     * Запись значения в кэш *Не используется!*
     *
     * @throws GeneralException
     * {RU}
     *
     * {EN}
     * Cache write *Doesn't use!*
     *
     * @throws GeneralException
     * {EN}
     */
    override fun write(datatype: Datatype, data: ULong) =
        throw GeneralException("You can't write in CachedMemoryStream!")

    /**
     * {RU}
     * Чтение значения из кэша без изменения текущей позиции
     *
     * @param datatype тип данных для загрузки
     * @return вычитанное значение
     * {RU}
     *
     * {EN}
     * Read value from cache without changing count of read bytes
     *
     * @param datatype data type to read
     * @return read value
     * {EN}
     */
    override fun peek(datatype: Datatype) = peekCache(datatype.bytes)

    /**
     * {EN}Reset current count of read bytes from stream{EN}
     *
     * {RU}Откат текущей позиции к исходному состоянию{RU}
     */
    override fun rewind() {
        // TODO: use ByteBuffer's internal position
        readBytes = 0
    }

    override val offset get() = (readBytes.ulong_z - mark).int

    override val data: ByteArray
        get() {
            cache.position(0)
            cache.limit(readBytes)
            return cache.slice().let {
                if (it.hasArray()) {
                    it.array()
                } else {
                    val newArray = ByteArray(readBytes)
                    it.get(newArray)
                    newArray
                }
            }.also {
                cache.limit(cache.capacity())
            }
        }
}

fun ByteBuffer.getNumber(position: Int, size: Int): ULong =
    if (order() != LITTLE_ENDIAN) {
        throw IllegalStateException("Unable to use the ByteBuffer.getNumber with not LE ByteBuffer (Not Implemented)");
    } else {
        when (size) {
            1 -> this.get(position).ulong_z
            2 -> this.getShort(position).ulong_z
            3 -> this.getChar(position).byte.ulong_z or (this.getShort(position + 1).ulong_z shl 8)
            4 -> this.getInt(position).ulong_z
            5 -> this.getChar(position).byte.ulong_z or (this.getInt(position + 1).ulong_z shl 8)
            6 -> this.getShort(position).ulong_z or (this.getInt(position + 2).ulong_z shl 16)
            7 -> {
                // TODO: optimize using read of 8 bytes, if the borders allows
                this.getChar(position).byte.ulong_z or
                        (this.getShort(position + 1).ulong_z shl 8) or
                        (this.getInt(position + 3).ulong_z shl 24)
            }

            8 -> {
                this.getLong(position).ulong
            }

            else -> throw IllegalArgumentException("Wrong ByteBuffer.getNumber size: $size at position $position")
        }
    }

fun ByteBuffer.putNumber(position: Int, value: ULong, size: Int): Unit {
    if (order() != LITTLE_ENDIAN) {
        throw IllegalStateException("Unable to use the ByteBuffer.getNumber with not LE ByteBuffer (Not Implemented)");
    } else when (size) {
        1 -> this.put(position, value.byte)
        2 -> this.putShort(position, value.short)
        3 -> {
            this.putChar(position, value.byte.char)
            this.putShort(position + 1, value[23..8].short)
        }

        4 -> this.putInt(position, value.int)
        5 -> {
            this.putChar(position, value.byte.char)
            this.putInt(position + 1, value[39..8].int)
        }

        6 -> {
            this.putShort(position, value.short)
            this.putInt(position + 2, value[47..16].int)
        }

        7 -> {
            this.putChar(position, value.byte.char)
            this.putShort(position + 1, value[23..8].short)
            this.putInt(position + 3, value[55..24].int)
        }

        8 -> this.putLong(position, value.long)

        else -> throw IllegalArgumentException("Wrong ByteBuffer.getNumber size: $size at position $position")
    }
}