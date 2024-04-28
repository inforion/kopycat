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

import ru.inforion.lab403.common.extensions.hex16
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.interfaces.ISerializable
import ru.inforion.lab403.kopycat.serializer.storeValues
import java.util.*
import kotlin.math.max
import kotlin.math.min

typealias RingProtectionMap = MutableMap<Int, ULongRange>

/**
 * Класс анализатора стека
 */
open class StackAnalyzer(
    /**
     * Core with necessary registers and call detectors
     */
    val analyzerCore: StackAnalyzerCore,

    /**
     * Capture all stack difference, regardless of call detector result
     */
    val captureAll: Boolean = false,

    /**
     * Additional initialization for SP ranges within certain CPU protection rings
     */
    ringProtectionInit: RingProtectionMap.() -> Unit = {}
) : ISerializable {
    /**
     * Отношение Stack Pointer'а и данных.
     */
    val spToData = TreeMap<ULong, StackAnalyzerData>()

    /**
     * Данные с предыдущего шага
     */
    private lateinit var previousData: StackAnalyzerRegsData

    private val fallbackRingProtection = (
            analyzerCore.STACK_MIN_GROW_ADDRESS
                    ..
                    analyzerCore.STACK_MAX_GROW_ADDRESS
            )

    /**
     * SP ranges within certain CPU protection rings
     */
    val ringProtection: RingProtectionMap = mutableMapOf(
        0 to fallbackRingProtection
    ).also {
        ringProtectionInit(it)
    }

    private fun getRingProtection(ring: Int) = ringProtection.getOrDefault(ring, fallbackRingProtection)

    private fun removeTailSpToDataUnordered(ring: Int, fromSp: ULong, toSp: ULong) {
        return removeTailSpToData(ring, min(fromSp, toSp), max(fromSp, toSp))
    }

    private fun removeTailSpToData(ring: Int, fromSp: ULong, toSp: ULong) {
        require(fromSp < toSp) { "fromSp=0x${fromSp.hex16} should be lower than toSp=0x${toSp.hex16}" }

        val ringProtection = getRingProtection(ring)
        val startEntry = spToData.ceilingKey(fromSp) ?: min(ringProtection.first, ringProtection.last);
        val endEntryExcl = spToData.ceilingKey(toSp) ?: max(ringProtection.first, ringProtection.last);

        val keys = spToData.subMap(startEntry, endEntryExcl).keys.toSet()
        keys.forEach {
            spToData.remove(it)
        }
    }

    /**
     * Capture current state from stack analyzer's core
     */
    fun captureShot() {
        val currentData = StackAnalyzerRegsData(
            sp = analyzerCore.sp,
            pc = analyzerCore.pc,
            ra = analyzerCore.ra,
            ring = analyzerCore.ring,
            time = analyzerCore.time,
        )
        if (::previousData.isInitialized && (previousData.sp != currentData.sp || previousData.ra != currentData.ra)) {
            captureShotInternal(currentData, previousData)
        }

        if (analyzerCore.canBeSaved(currentData)) {
            previousData = currentData
        }
    }

    fun captureShotInternal(current: StackAnalyzerRegsData, previous: StackAnalyzerRegsData) {
        if (analyzerCore.isCallPerhaps(current, previous) || captureAll) {
            // Удаляем оставшийся от других вызовов хвост
            removeTailSpToDataUnordered(current.ring, getRingProtection(current.ring).last, current.sp)

            if (current.ring != previous.ring) {
                removeTailSpToDataUnordered(previous.ring, getRingProtection(current.ring).last, previous.sp)
            }

            spToData[current.sp] = StackAnalyzerData(current, previous)
            return
        }

        if (analyzerCore.isReturnPerhaps(current, previous) && current.ring == previous.ring) {
            // Возврат => нужно почистить стек
            removeTailSpToDataUnordered(current.ring, current.sp, previous.sp)
        }
    }

    /**
     * Clears analyzer's data
     */
    fun clear() = spToData.clear()

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> = super.serialize(ctxt) + storeValues(
        "spToData" to spToData.mapValues { (_, v) -> v.serialize() },
    ) + if (::previousData.isInitialized) {
        storeValues(
            "previousData" to previousData.serialize(),
        )
    } else {
        mapOf()
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)

        spToData.clear()
        snapshot["spToData"]?.let {
            spToData.putAll(
                (it as Map<String, Map<String, Any>>)
                    .mapValues { (_, v) -> StackAnalyzerData.deserializeToInstance(v) }
                    .mapKeys { (k, _) -> k.toULong(10) }
            )
        }

        snapshot["previousData"]?.let {
            previousData = StackAnalyzerRegsData.deserializeToInstance(it as Map<String, Any>)
        }
    }
}

