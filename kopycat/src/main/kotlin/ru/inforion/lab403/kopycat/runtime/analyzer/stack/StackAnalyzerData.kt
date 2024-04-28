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

import ru.inforion.lab403.common.extensions.cast
import ru.inforion.lab403.kopycat.serializer.loadValue

/**
 * Данные для анализатора стека
 */
data class StackAnalyzerData(
    val current: StackAnalyzerRegsData,
    val previous: StackAnalyzerRegsData,
) {
    companion object {
        fun deserializeToInstance(data: Map<String, Any>) = StackAnalyzerData(
            current = (data["current"] ?: emptyMap<String, Any>())
                .cast<Map<String, Any>>()
                .let { StackAnalyzerRegsData.deserializeToInstance(it) },
            previous = (data["previous"] ?: emptyMap<String, Any>())
                .cast<Map<String, Any>>()
                .let { StackAnalyzerRegsData.deserializeToInstance(it) },
        )
    }

    fun serialize(): Map<String, Any> = mapOf(
        "current" to current,
        "previous" to previous,
    )
}

data class StackAnalyzerRegsData(
    /**
     * Stack pointer
     */
    val sp: ULong,

    /**
     * First Program Counter with given Stack Pointer
     */
    val pc: ULong,

    /**
     * Return Address
     */
    val ra: ULong,

    /**
     * CPU Protection Ring
     */
    val ring: Int,

    /**
     * For sorting
     */
    val time: ULong,
) {
    companion object {
        fun deserializeToInstance(data: Map<String, Any>) = StackAnalyzerRegsData(
            sp = loadValue(data, "sp") { 0uL },
            pc = loadValue(data, "pc") { 0uL },
            ra = loadValue(data, "ra") { 0uL },
            ring = loadValue<Int>(data, "ring") { 0 },
            time = loadValue(data, "time") { 0uL },
        )
    }

    fun serialize(): Map<String, Any> = mapOf(
        "sp" to sp,
        "pc" to pc,
        "ra" to ra,
        "ring" to ring,
        "time" to time,
    )
}