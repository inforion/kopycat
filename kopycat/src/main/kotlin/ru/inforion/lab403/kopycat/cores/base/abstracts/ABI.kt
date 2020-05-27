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

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.common.StackStream
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import java.util.logging.Level

/**
 * {RU}
 * Класс двоичного интерфеса приложений (Application Binary Interface, ABI).
 * Используется для абстрагирования взаимодействия ОС с конкретной реализацией аппаратного обеспечения.
 *
 * @param T шаблон типа AGenericCore
 * @property core ядро
 * @property heap размер кучи
 * @property stack размер стека
 * @property bigEndian порядок байтов
 * @property types поддерживаемые типы данных
 * @property sp регистр указателя стека
 * @property ssr регистр указателя сегмента стека
 * @property ra регистр адреса возврата (при отсутствии регистра необходимо генерировать ислючение и перегрузить метод getReturnAddress)
 * @property v0 регистр результата
 * @property argl список аргументов, передаваемых через регистры
 * @constructor создаёт абстрактный класс двоичного интерфейса
 * {RU}
 */
abstract class ABI<T: AGenericCore>(
        val core: T,
        val heap: LongRange,
        val stack: LongRange,
        val bigEndian: Boolean,
        val types: Types = Types.default) {

    companion object {
        val log = logger(Level.INFO)
    }

    /**
     * {RU}
     * Типы данных, поддерживаемые конкретным ABI
     * {RU}
     */
    data class Types(
            val byte: Datatype,
            val half: Datatype,
            val word: Datatype,
            val pointer: Datatype,
            val long: Datatype) {
        companion object {
            val default = Types(BYTE, WORD, DWORD, DWORD, QWORD)
        }
    }

    abstract val ssr: Int
    abstract val sp: ARegister<T>   // stack pointer register
    abstract val ra: ARegister<T>   // return address register (if not possible error should be thrown and getReturnAddress override)
    abstract val v0: ARegister<T>   // return value register
    abstract val argl: List<ARegister<T>>   // list of register arguments

    abstract fun createCpuContext(): AContext<*>

    var stackPointerValue: Long
        get() = sp.value(core)
        set(value) = sp.value(core, value)

    var returnValue: Long
        get() = v0.value(core)
        set(value) = v0.value(core, value)

    var programCounterValue: Long
        get() = core.cpu.pc
        set(value) { core.cpu.pc = value }

    open var returnAddressValue: Long
        get() = ra.value(core)
        set(value) = ra.value(core, value)

    open fun ret() {
        programCounterValue = returnAddressValue
    }

    /**
     * {RU}
     * Получает аргументы для подпрограммы (функции) по списку типов [args]
     * @return массив аргументов функции
     * {RU}
     */
    open fun getArgs(args: Array<ArgType>): Array<Long> {
        var res = argl.map { it.value(core) }

        if (args.size > argl.size) {
            val ss = stackStream()
            res += args.drop(argl.size).map {  // !!!!!!!!!!!!!!!!!!!!!  Не все аргументы !!!!!!!!!!!!!!1
                when (it) {
                    ArgType.Pointer -> ss.read(types.pointer)
                    ArgType.Word -> ss.read(types.word)
                    ArgType.Half -> ss.read(types.half)
                                                               // x86 can't push byte, but others ...
                    ArgType.Byte -> ss.read(types.half)  // x86 не поддерживает помещение 1 байта на стек
                }
            }
        }

        return res.toTypedArray()
    }

    /**
     * {RU}
     * Получает [n] аргументов одинакового типа [type] для подпрограммы (функции)
     * @return массив аргументов функции
     * {RU}
     */
    open fun getArgs(n: Int, type: ArgType): Array<Long> = getArgs(Array(n) { type })

    /**
     * {RU}
     * Установка аргументов [args] перед вызовом функции
     * {RU}
     */
    open fun setArgs(args: Array<Long>) {
        val n = minOf(args.size, argl.size)

        (0 until n).forEach { i -> argl[i].value(core, args[i]) }

        if (args.size > argl.size)
            args.drop(argl.size).asReversed().forEach { push(it) }
    }

    /**
     * {RU}
     * Сохранение значения [value] типа [datatype] на стеке
     * (для x86 необходимо переопределить реализацию метода)
     * x86 must have its own push
     * {RU}
     */
    open fun push(value: Long, datatype: Datatype = types.word) {
        stackPointerValue -= datatype.bytes
        writeMemory(stackPointerValue, value, datatype)
    }

    /**
     * {RU}
     * Получение значения типа [datatype] со стека
     * @return значение с вершины стека
     * {RU}
     */
    open fun pop(datatype: Datatype = types.word): Long {
        val result = readMemory(stackPointerValue, datatype)
        stackPointerValue += datatype.bytes
        return result
    }

    /**
     * {RU}
     * Сброс регистров общего назначения
     * {RU}
     */
    fun cpuReset() = core.cpu.reset()

    /**
     * {RU}
     * Получение доступа к стеку, как к потоку данных
     * @return объект класса StackStream
     * {RU}
     */
    fun stackStream(opSize16bit: Boolean = false, where: Long = stackPointerValue) =
            StackStream(core.cpu.ports.mem, where, ssr, opSize16bit)

    abstract fun gpr(index: Int): ARegister<T>

    /**
     * {RU}
     * Запись значения [value] в регистр общего назначения с индексом [index]
     * {RU}
     */
    fun writeRegister(index: Int, value: Long) { gpr(index).value(core, value) }

    /**
     * {RU}
     * Чтение значения регистра общего назначения с индексом [index]
     * @return значение регистра
     * {RU}
     */
    fun readRegister(index: Int): Long = gpr(index).value(core)

    /**
     * {RU}
     * Запись массива байт [data] в память по адресу [address]
     * {RU}
     */
    open fun writeBytes(address: Long, data: ByteArray) { core.store(address, data) }

    /**
     * {RU}
     * Чтение массива байт размером [size] из памяти по адресу [address]
     * @return массив байт
     * {RU}
     */
    open fun readBytes(address: Long, size: Int): ByteArray = core.load(address, size)

    /**
     * {RU}
     * Запись значения [value] типа [datatype] в память по адресу [address]
     * {RU}
     */
    fun writeMemory(address: Long, value: Long, datatype: Datatype) { core.write(datatype, address, value) }

    /**
     * {RU}
     * Чтение значения типа [datatype] из памяти по адресу [address]
     * @return значение из памяти
     * {RU}
     */
    fun readMemory(address: Long, datatype: Datatype) = core.read(datatype, address)

    /**
     * {RU}
     * Запись указателя [value] в память по адресу [address]
     * {RU}
     */
    fun writePointer(address: Long, value: Long) { core.write(types.pointer, address, value) }

    /**
     * {RU}
     * Чтение указателя из памяти по адресу [address]
     * @return значение из памяти
     * {RU}
     */
    fun readPointer(address: Long): Long = core.read(types.pointer, address)

    /**
     * {RU}
     * Запись long-значения [value] в память по адресу [address]
     * {RU}
     */
    fun writeLong(address: Long, value: Long) { core.write(types.long, address, value) }

    /**
     * {RU}
     * Чтение long-значения из памяти по адресу [address]
     * @return long-значение из памяти
     * {RU}
     */
    fun readLong(address: Long): Long = core.read(types.long, address)

    /**
     * {RU}
     * Запись word-значения [value] в память по адресу [address]
     * {RU}
     */
    fun writeWord(address: Long, value: Long) { core.write(types.word, address, value) }

    /**
     * {RU}
     * Чтение word-значения из памяти по адресу [address]
     * @return word-значение из памяти
     * {RU}
     */
    fun readWord(address: Long): Long = core.read(types.word, address)

    /**
     * {RU}
     * Запись half-значения [value] в память по адресу [address]
     * {RU}
     */
    fun writeHalf(address: Long, value: Long) { core.write(types.half, address, value) }

    /**
     * {RU}
     * Чтение half-значения из памяти по адресу [address]
     * @return half-значение из памяти
     * {RU}
     */
    fun readHalf(address: Long): Long = core.read(types.half, address)

    /**
     * {RU}
     * Запись byte-значения [value] в память по адресу [address]
     * {RU}
     */
    fun writeByte(address: Long, value: Long) { core.write(types.byte, address, value) }

    /**
     * {RU}
     * Чтение byte-значения из памяти по адресу [address]
     * @return byte-значение из памяти
     * {RU}
     */
    fun readByte(address: Long): Long = core.read(types.byte, address)
}