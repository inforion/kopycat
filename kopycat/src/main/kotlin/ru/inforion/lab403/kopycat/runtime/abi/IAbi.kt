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
package ru.inforion.lab403.kopycat.runtime.abi

import ru.inforion.lab403.common.extensions.byte
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.interfaces.IValuable
import ru.inforion.lab403.kopycat.runtime.funcall.FunArg
import ru.inforion.lab403.kopycat.runtime.funcall.StackAllocation
import kotlin.math.min

@Suppress("INAPPLICABLE_JVM_NAME")
interface IAbi {
    val core: ACore<*, *, *>

    // TODO: maybe IValuable?

    @get:JvmName("getSP")
    @set:JvmName("setSP")
    var sp: ULong

    @get:JvmName("getPC")
    @set:JvmName("setPC")
    var pc: ULong

    @get:JvmName("getSS")
    val ss: Int

    /**
     * Выравнивание стека (чему должен быть кратен адрес SP).
     */
    fun getStackAlignment(): Datatype

    /**
     * Увеличить адрес стека.
     * Если стек растёт вниз, то это вычитание. Если вверх, то сложение.
     *
     * Функция ожидает выравненный размер!
     */
    fun growStackAddress(addr: ULong, alignedSize: ULong): ULong

    /**
     * Уменьшить адрес стека.
     * Если стек растёт вниз, то это сложение. Если вверх, то сложение.
     *
     * Функция ожидает выравненный размер!
     */
    fun shrinkStackAddress(addr: ULong, alignedSize: ULong): ULong

    /**
     * TODO: написать доку
     * Подчеркнуть, что количество аргументов передаётся без вычитания getRegisterArgsAmount
     */
    fun allocArgsOnStack(argsAmount: Int): StackAllocation

    /**
     * TODO: написать доку
     */
    fun saveState(): List<ULong>

    /**
     * TODO: написать доку
     */
    fun restoreState(state: List<ULong>)

    /**
     * TODO: написать доку
     */
    fun getRegisterArgsAmount(): Int

    /**
     * IValuable argument register.
     * Indexes from zero.
     */
    fun argRegister(i: Int): IValuable?

    /**
     * Get function result
     */
    fun getResult(i: Int = 0): ULong

    /**
     * Set function result
     */
    fun setResult(i: Int, value: ULong)

    /**
     * Выравнивание стека (чему должен быть кратен адрес SP).
     * В байтах
     */
    fun getStackAlignmentSize() = getStackAlignment().bytes

    /**
     * Раскладывает аргументы по регистрам и, если их не хватает, аллоцирует место на стеке
     */
    fun allocAndPutArgs(vararg args: FunArg) = allocAndPutArgs(args.toList())

    // TODO: как-то всё намешано. Мб инкапсулировать те методы, которые без имплементации

    /**
     * Выравнять размер аллокации на стеке
     */
    fun alignStack(size: ULong): ULong {
        val alignment = getStackAlignmentSize().ulong_z
        val floored = (size / alignment) * alignment
        return if (floored == size) size else floored + alignment
    }

    /**
     * Performs an aligned stack allocation
     *
     * Изменяет состояния регистров Core,
     * в частности, регистра, содержащего SP
     */
    fun allocOnStack(size: ULong): StackAllocation {
        require(size <= 0x1024uL) { "Large stack memory allocation (> 1KiB) causes bugs" }

        val alignedSize = alignStack(size)

        val oldSp = sp
        val newSp = growStackAddress(oldSp, alignedSize)
        sp = newSp

        return StackAllocation(
            oldSp,
            newSp,
            alignedSize,
            min(newSp, oldSp)
        )
    }

    /**
     * Performs an aligned stack clearance.
     * SP должен совпадать с конечным SP в аллокации
     *
     * Изменяет состояния регистров Core,
     * в частности, регистра, содержащего SP
     */
    fun clearStackAllocation(allocation: StackAllocation) {
        require(allocation.newSP == sp) {
            "Allocation RSP (0x${allocation.newSP.hex}) " +
                    "does not equal to Core SP (0x${sp.hex})"
        }

        sp = shrinkStackAddress(sp, allocation.size)
    }

    /**
     * Function result must be used
     */
    fun putOnStack(value: String): StackAllocation {
        val byteArray = value.toByteArray() + 0x00.byte
        return putOnStack(byteArray)
    };

    /**
     * Function result must be used
     */
    fun putOnStack(value: ByteArray): StackAllocation =
        allocOnStack(value.size.ulong_z).also { allocated ->
            core.store(
                allocated.address,
                value,
                ss
            )
        }

    /**
     * Function result must be used
     */
    fun putOnStack(value: ULong, size: Int): StackAllocation = allocOnStack(size.ulong_z).also { allocated ->
        core.write(
            allocated.address,
            ss,
            size,
            value
        )
    }

    /**
     * Unsafe function to push one value (with minimal aligned size) into the stack
     */
    fun pushStack(value: ULong) {
        putOnStack(value, getStackAlignmentSize())
    }

    /**
     * Unsafe function to pop one value (with minimal aligned size) from the stack
     */
    fun popStack(): ULong {
        val size = getStackAlignmentSize()

        val oldSp = sp
        val newSp = shrinkStackAddress(oldSp, size.ulong_z)
        sp = newSp

        return core.read(oldSp, ss, size)
    }

    /**
     * Gets i-th element from the top of the stack.
     * Indexes from the zero
     */
    fun getTopStack(i: Int): ULong {
        val alignment = getStackAlignment()

        return core.read(
            shrinkStackAddress(sp, alignment.bytes.ulong_z * i.ulong_z),
            ss,
            alignment.bytes
        )
    }

    /**
     * Sets i-th element from the top of the stack.
     * Indexes from the zero
     */
    fun setTopStack(i: Int, value: ULong) {
        val alignment = getStackAlignment()
        core.write(
            shrinkStackAddress(sp, alignment.bytes.ulong_z * i.ulong_z),
            ss,
            alignment.bytes,
            value
        )
    }

    /**
     * TODO: написать доку
     */
    fun allocAndPutArgs(args: List<FunArg>): List<StackAllocation> {
        val (allocasData, preparedArgs) = args
            .map {
                when (it) {
                    is FunArg.String -> putOnStack(it.value)
                        .let { alloca -> alloca to alloca.address }

                    is FunArg.ByteArray -> putOnStack(it.value)
                        .let { alloca -> alloca to alloca.address }

                    is FunArg.Pointer -> null to it.value
                    is FunArg.Number -> null to it.value
                }
            }
            .unzip()
        val argsAlloca = allocArgsOnStack(args.size)
        val resultAllocs = allocasData.filterNotNull() + listOf(argsAlloca)

        preparedArgs.forEachIndexed { i, value -> setArgument(i, value) }
        return resultAllocs
    }

    /**
     * Indexes from zero.
     */
    // TODO: индексация может промахнуться мимо аллокации
    fun setArgument(i: Int, value: ULong) {
        argRegister(i)?.also {
            it.data = value
        } ?: (i - getRegisterArgsAmount()).let { stackPos ->
            require(stackPos >= 0) {
                "Stack position must be positive " +
                        "i=${i} getRegisterArgsAmount=${getRegisterArgsAmount()}"
            }
            setTopStack(stackPos, value)
        }
    }

    /**
     * Indexes from zero.
     */
    // TODO: индексация может промахнуться мимо аллокации
    fun getArgument(i: Int): ULong =
        argRegister(i)?.data ?: (i - getRegisterArgsAmount()).let { stackPos ->
            require(stackPos >= 0) {
                "Stack position must be positive " +
                        "i=${i} getRegisterArgsAmount=${getRegisterArgsAmount()}"
            }
            getTopStack(stackPos)
        }

    fun call(startAddress: ULong)
}