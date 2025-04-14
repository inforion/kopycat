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
package ru.inforion.lab403.kopycat

import org.openjdk.jmh.annotations.*
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.random.nextInt

@Fork(2)
@Warmup(iterations = 1, time = 5)
@Measurement(iterations = 2, time = 5)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Suppress("unused")
open class BusNRegisters {
    private lateinit var tester: Tester<BusPerfTestTop>
    private lateinit var random: Array<Int>

    @Param("100", "1000")
    open var n: Int = 0

    @Setup fun setup() {
        tester = Tester(
            BusPerfTestTop().apply {
                repeat(n) { i ->
                    this.Register(
                        ports.port1,
                        i.ulong_z * 4uL,
                        Datatype.DWORD,
                        "reg$i",
                    )
                }
            },
        )

        random = Array(n) { Random.nextInt(0 until n) }
    }

    @TearDown fun teardown() = tester.close()

    private inline fun sequential(crossinline fn: (Int) -> Unit) = repeat(n) { fn(it) }
    private inline fun repeated(crossinline fn: (Int) -> Unit) = repeat(n / 5) { i -> repeat(5) { fn(i) } }
    private inline fun random(crossinline fn: (Int) -> Unit) = random.forEach { fn(it) }

    private fun read(i: Int) = tester.top.ports.port2.read(i.ulong_z * 4uL, 0, 4)
    private fun write(i: Int) = tester.top.ports.port2.write(i.ulong_z * 4uL, 0, 4, 0xDEAD_BEEFuL)

    @Benchmark fun sequentialReadDword() = sequential(::read)
    @Benchmark fun sequentialWriteDword() = sequential(::write)
    @Benchmark fun repeatedReadDword() = repeated(::read)
    @Benchmark fun repeatedWriteDword() = repeated(::write)
    @Benchmark fun randomReadDword() = random(::read)
    @Benchmark fun randomWriteDword() = random(::write)
}
