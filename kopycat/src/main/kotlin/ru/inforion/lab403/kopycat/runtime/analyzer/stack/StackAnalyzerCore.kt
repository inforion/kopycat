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
package ru.inforion.lab403.kopycat.runtime.analyzer.stack

import ru.inforion.lab403.kopycat.cores.base.abstracts.ACPU

/**
 * Интерфейс анализируемого
 */
@Suppress("INAPPLICABLE_JVM_NAME")
interface StackAnalyzerCore {
    val cpu: ACPU<*, *, *, *>

    /**
     * Stack Pointer
     */
    @get:JvmName("getSp")
    val sp: ULong

    /**
     * Program Counter.
     * Адрес исполняемой инструкции
     */
    @get:JvmName("getPc")
    val pc: ULong

    /**
     * Адрес возврата из текущей процедуры
     */
    @get:JvmName("getRa")
    val ra: ULong

    /**
     * Кольцо защиты процессора.
     * Если отсутствует или не предусмотрено, то 0
     */
    @get:JvmName("getRing")
    val ring: Int
        get() = 0

    /**
     * Абсолютное время, увеличивающееся с каждой выполненной инструкцией
     */
    @get:JvmName("getTime")
    val time: ULong
        get() = sp

    /**
     * Конечный адрес стека. Если растёт вниз, то 0.
     */
    val STACK_MAX_GROW_ADDRESS: ULong

    /**
     * Начальный адрес стека. Если растёт вниз, то 0xFFFF_FFFF_FFFF_FFFF.
     */
    val STACK_MIN_GROW_ADDRESS: ULong

    fun isCallPerhaps(current: StackAnalyzerRegsData, previous: StackAnalyzerRegsData): Boolean

    fun isReturnPerhaps(current: StackAnalyzerRegsData, previous: StackAnalyzerRegsData): Boolean

    fun canBeSaved(current: StackAnalyzerRegsData): Boolean = true
}
