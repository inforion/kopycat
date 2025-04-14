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

import java.util.*

internal class RangeMultiMap<V> private constructor(private val map: TreeMap<ULongRange, MutableList<V>>) {
    constructor() : this(TreeMap { a, b -> a.first.compareTo(b.first) })

    override fun toString() = map.toString()

    operator fun get(k: ULong) = map.floorEntry(k..k)?.let { entry ->
        if (entry.key.contains(k)) {
            entry.value
        } else {
            null
        }
    }

    private operator fun get(k: ULongRange) = map[k]

    private fun ULongRange.overlap(other: ULongRange): ULongRange? {
        val maxStart = maxOf(start, other.first)
        val minEnd = minOf(endInclusive, other.last)

        return if (maxStart <= minEnd) {
            maxStart..minEnd
        } else {
            null
        }
    }

    private fun ULongRange.closedSub(ranges: Iterable<ULongRange>) = ranges.fold(mutableListOf(this)) { result, range ->
        result.fold(mutableListOf()) { acc, r ->
            if (range.last < r.first || range.first > r.last) {
                acc.add(r)
            } else if (range.first >= r.first && range.last <= r.last) {
                if (r.first != range.first) {
                    val add = r.first..range.first - 1uL
                    if (add.last >= add.first) {
                        acc.add(add)
                    }
                }

                if (r.last != range.last) {
                    val add = range.last + 1u..r.last
                    if (add.last >= add.first) {
                        acc.add(add)
                    }
                }
            } else if (range.first <= r.first && range.last <= r.last) {
                if (range.last != r.last) {
                    acc.add(range.last + 1u..r.last)
                }
            } else if (range.first >= r.first && range.last >= r.last) {
                if (r.first != range.first) {
                    acc.add(r.first..range.first - 1u)
                }
            }
            acc
        }
    }

    private sealed class AddPlan<V> {
        data class Delete<V>(val interval: ULongRange) : AddPlan<V>()
        data class Create<V>(val interval: ULongRange, val value: MutableList<V>) : AddPlan<V>()
        data class Append<V>(val interval: ULongRange, val value: MutableList<V>) : AddPlan<V>()
    }

    fun add(k: ULongRange, v: Iterable<V>) {
        val overlaps = map.keys
            .asSequence()
            .takeWhile { it.first <= k.last }
            .mapNotNull {
                if (k.overlap(it) != null) {
                    it
                } else {
                    null
                }
            }
            .toList()

        (
            k.closedSub(overlaps).map {
                AddPlan.Create(it, v.toMutableList())
            } + overlaps.fold(mutableListOf<AddPlan<V>>()) { acc, currentRange ->
                val overlap = k.overlap(currentRange)
                if (overlap != null) {
                    val left = currentRange.first < k.first
                    val right = currentRange.last > k.last

                    if (left || right) {
                        val value = map[currentRange]!!
                        acc.add(AddPlan.Delete(currentRange))
                        if (left) {
                            acc.add(AddPlan.Create(currentRange.first..k.first - 1uL, value))
                        }
                        if (right) {
                            acc.add(AddPlan.Create(k.last + 1uL..currentRange.last, value))
                        }

                        acc.add(
                            AddPlan.Create(
                                overlap,
                                value.toMutableList().also {
                                    it.addAll(v)
                                },
                            ),
                        )
                    } else {
                        acc.add(AddPlan.Append(overlap, v.toMutableList()))
                    }
                }

                acc
            }
        ).forEach { step ->
            when (step) {
                is AddPlan.Append<V> -> this[step.interval]!!.addAll(step.value)
                is AddPlan.Create<V> -> map[step.interval] = step.value
                is AddPlan.Delete -> map.remove(step.interval)
            }
        }
    }

    fun add(k: ULongRange, v: V) = add(k, listOf(v))

    fun mapNotNull(fn: (V) -> V?) = RangeMultiMap(
        TreeMap(
            map.entries.mapNotNull { entry ->
                val newValue = entry.value.mapNotNull(fn).toMutableList()
                if (newValue.isNotEmpty()) {
                    entry.key to newValue
                } else {
                    null
                }
            }.toMap().toSortedMap { a, b -> a.first.compareTo(b.first) },
        )
    )

    fun addAll(other: RangeMultiMap<V>) {
        other.map.entries.forEach {
            add(it.key, it.value)
        }
    }

    fun clear() = map.clear()
    fun isNotEmpty() = map.isNotEmpty()
    fun values() = map.values.flatten()
}
