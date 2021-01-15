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
package ru.inforion.lab403.kopycat.cores.base.abstracts

import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister


abstract class ABI<T: AGenericCore> constructor(
        val core: T,
        bits: Int,
        bigEndian: Boolean,
        types: Types = Types.default): ABIBase(bits, bigEndian, types) {

    abstract val pc: ARegister<T>   // program counter register
    abstract val sp: ARegister<T>   // stack pointer register
    abstract val ra: ARegister<T>   // return address register
    abstract val rv: ARegister<T>   // return value register

    open val segmentSelector = 0

    abstract val registerCount: Int
    abstract fun register(index: Int): ARegister<T>

    abstract fun createContext(): AContext<T>

    override var stackPointerValue: Long
        get() = sp.value(core)
        set(value) = sp.value(core, value)

    override val returnValue: Long
        get() = rv.value(core)

    override var programCounterValue: Long
        get() = pc.value(core)
        set(value) { pc.value(core, value) }

    override var returnAddressValue: Long
        get() = ra.value(core)
        set(value) = ra.value(core, value)

    override fun setReturnValue(value: Long, type: Datatype, instance: ABIBase) {
        if (type.bits > bits)
            throw NotImplementedError("Override this function for ${type.bits}-bit arguments")
        instance.writeRegister(rv.reg, value like type)
    }

    override fun readRegister(index: Int): Long = register(index).value(core)

    override fun writeRegister(index: Int, value: Long) = register(index).value(core, value)

    override fun readStack(offset: Long, type: Datatype) =
            core.read(type, stackPointerValue + offset, segmentSelector)

    override fun writeStack(offset: Long, type: Datatype, value: Long) =
            core.write(type, stackPointerValue + offset, value, segmentSelector)

    /**
     * {RU}
     * Запись массива байт [data] в память по адресу [address]
     * {RU}
     */
    fun writeBytes(address: Long, data: ByteArray) = run { core.store(address, data) }

    /**
     * {RU}
     * Чтение массива байт размером [size] из памяти по адресу [address]
     * @return массив байт
     * {RU}
     */
    fun readBytes(address: Long, size: Int) = core.load(address, size)

    /**
     * {RU}
     * Запись значения [value] типа [type] в память по адресу [address]
     * {RU}
     */
    fun writeMemory(address: Long, value: Long, type: Datatype) = run { core.write(type, address, value) }

    /**
     * {RU}
     * Чтение значения типа [type] из памяти по адресу [address]
     * @return значение из памяти
     * {RU}
     */
    fun readMemory(address: Long, type: Datatype) = core.read(type, address)

    /**
     * {RU}
     * Запись указателя [value] в память по адресу [address]
     * {RU}
     */
    fun writePointer(address: Long, value: Long) = run { core.write(types.pointer, address, value) }

    /**
     * {RU}
     * Чтение указателя из памяти по адресу [address]
     * @return значение из памяти
     * {RU}
     */
    fun readPointer(address: Long) = core.read(types.pointer, address)

    /**
     * {RU}
     * Запись long long-значения [value] в память по адресу [address]
     * {RU}
     */
    fun writeLongLong(address: Long, value: Long) = run { core.write(types.longLong, address, value) }

    /**
     * {RU}
     * Чтение long long-значения из памяти по адресу [address]
     * @return long long-значение из памяти
     * {RU}
     */
    fun readLongLong(address: Long) = core.read(types.longLong, address)

    /**
     * {RU}
     * Запись long long-значения [value] в память по адресу [address]
     * {RU}
     */
    fun writeLong(address: Long, value: Long) = run { core.write(types.long, address, value) }

    /**
     * {RU}
     * Чтение long-значения из памяти по адресу [address]
     * @return long-значение из памяти
     * {RU}
     */
    fun readLong(address: Long) = core.read(types.long, address)

    /**
     * {RU}
     * Запись int-значения [value] в память по адресу [address]
     * {RU}
     */
    fun writeInt(address: Long, value: Long) = run { core.write(types.int, address, value) }

    /**
     * {RU}
     * Чтение int-значения из памяти по адресу [address]
     * @return int-значение из памяти
     * {RU}
     */
    fun readInt(address: Long) = core.read(types.int, address)

    /**
     * {RU}
     * Запись short-значения [value] в память по адресу [address]
     * {RU}
     */
    fun writeShort(address: Long, value: Long) = run { core.write(types.short, address, value) }

    /**
     * {RU}
     * Чтение short-значения из памяти по адресу [address]
     * @return short-значение из памяти
     * {RU}
     */
    fun readShort(address: Long) = core.read(types.short, address)

    /**
     * {RU}
     * Запись char-значения [value] в память по адресу [address]
     * {RU}
     */
    fun writeChar(address: Long, value: Long) = run { core.write(types.char, address, value) }

    /**
     * {RU}
     * Чтение char-значения из памяти по адресу [address]
     * @return char-значение из памяти
     * {RU}
     */
    fun readChar(address: Long) = core.read(types.char, address)
}