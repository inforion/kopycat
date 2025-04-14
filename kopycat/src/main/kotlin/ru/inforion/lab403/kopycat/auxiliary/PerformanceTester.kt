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
package ru.inforion.lab403.kopycat.auxiliary

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINER
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.common.Module
import java.io.Closeable
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

/**
 * {EN}
 * Helper class for integration testing and performance recording
 *
 *
 * @param exitPoint address when Kopycat should exit
 * @param maxExecute maximum execute instructions
 * @param makeTop creator of top module
 * {EN}
 */
class PerformanceTester<T: Module>(
        val exitPoint: ULong,
        val maxExecute: ULong = ULONG_MAX,
        connectionInfo: Boolean = false,
        makeTop: () -> T
): Closeable {
    companion object {
        @Transient private val log = logger(FINER)
    }

    data class Stats(val elapsed: Long = 0, val executed: ULong = 0u)

    val kopycat = Kopycat(null).apply {
        open(makeTop(), null, false)
        if (connectionInfo) printModulesConnectionsInfo()
    }

    override fun close() = kopycat.close()

    @Suppress("UNCHECKED_CAST")
    val top get() = kopycat.top as T

    private var passed = 0
    private var afterResetCblk: (top: T) -> Unit = { }
    private val oneShotTracePointCblks = mutableMapOf<ULong, (top: T) -> Unit>()
    private val alwaysTracePointClbks = mutableMapOf<ULong, (top: T) -> Unit>()

    private val stopRequest = AtomicBoolean(false)

    private var failed = false

    private fun stopCondition(step: ULong, core: AGenericCore): Boolean {
        if (step >= maxExecute) {
            log.severe { "Maximum instructions count execution reached ($step == $maxExecute)" }
            return true
        }

        if (core.pc == exitPoint) {
            log.finer { "[${core.pc.hex8}] '$core' reached exit point 0x${exitPoint.hex8}" }
            return true
        }

        if (stopRequest.get()) {
            log.finer { "[${core.pc.hex8}] '$core' requested to stop!" }
            return true
        }

        return false
    }

    private fun execute(message: String): Stats {
        failed = false
        stopRequest.set(false)

        log.fine { message }

        passed = 0
        kopycat.reset()

        afterResetCblk(top)

        var executed: ULong = 0u
        val elapsed = measureTimeMillis {
            executed = kopycat.run { step, core ->
                // execute one-shot trace point callback
                oneShotTracePointCblks.remove(core.pc)?.invoke(top)

                // execute always trace point callback
                alwaysTracePointClbks[core.pc]?.invoke(top)

                // check if stop required
                !stopCondition(step, core)
            }
        }

        if (failed || !stopRequest.get() && (kopycat.core.pc != exitPoint || kopycat.hasException())) {
            log.severe { "exitPoint is not reached, trace:" }
            kopycat.core.info.dump()
            throw IllegalStateException("Something goes wrong during test execution...")
        }

        return Stats(elapsed, executed)
    }

    fun stop(failed: Boolean) {
        stopRequest.set(true)
        this.failed = failed
    }

    /**
     * {EN}
     * Add callback to invoke exactly after emulator reset and before run
     *
     * @param block Callback to invoke
     *
     * @return self for chain access
     * {EN}
     */
    fun afterReset(block: (top: T) -> Unit) = apply { afterResetCblk = block }

    /**
     * {EN}
     * Add callback to invoke **once** when execution reach specified address.
     * NOTE: Callback will be invoked regardless [stopCondition] match or not.
     *
     * @param address Program Counter value when invoke callback
     * @param block Callback to invoke
     *
     * @return self for chain access
     * {EN}
     */
    fun atAddressOnce(address: ULong, block: (top: T) -> Unit) = apply {
        require(address !in oneShotTracePointCblks) { "Trace block already set for address ${address.hex8}" }
        oneShotTracePointCblks[address] = block
    }

    /**
     * {EN}
     * Add callback to invoke **all times** when execution reach specified address.
     * NOTE: Callback will be invoked regardless [stopCondition] match or not.
     *
     * @param address Program Counter value when invoke callback
     * @param block Callback to invoke
     *
     * @return self for chain access
     * {EN}
     */
    fun atAddressAlways(address: ULong, block: (top: T) -> Unit) = apply {
        require(address !in alwaysTracePointClbks) { "Trace block already set for address ${address.hex8}" }
        alwaysTracePointClbks[address] = block
    }

    /**
     * {EN}
     * Add callback to invoke **all times** when terminal on TCP [port] gets a byte.
     *
     * @param tty Path to device where wait for bytes
     * @param predicate Callback to invoke: input received char and return boolean when to stop
     *
     * @return self for chain access
     * {EN}
     */
    fun whenTerminalReceive(port: Int, predicate: (char: Char) -> Boolean): PerformanceTester<T> {
        thread {
            val stream = Socket("127.0.0.1", port).inputStream

            do {
                val char = stream.read().char
            } while (predicate(char))

            stop(false)
        }

        return this
    }

    /**
     * {EN}
     * Stops tester when terminal on TCP port [port] received specified string
     *
     * @param tty Path to device where wait for bytes
     * @param string Waiting string
     *
     * @return self for chain access
     * {EN}
     */
    fun stopWhenTerminalReceive(port: Int, string: String): PerformanceTester<T> {
        var currentIndex = 0
        whenTerminalReceive(port) {
            if (it == string[currentIndex])
                currentIndex++
            else
                currentIndex = 0

            currentIndex != string.length
        }
        return this
    }

    /**
     * {EN}
     * Run emulation under monitor condition. To stop emulation [exitPoint] can be set with required [passCount]
     * before interrupt. Also emulation may be stopped after reach specified number of [maxExecute] instructions.
     *
     * To perform action at given execution point see [atAddressOnce], [atAddressAlways] and [afterReset]
     *
     * @return self for chain access
     * {EN}
     */
    fun run(count: Int, warm: Int = 5) {
        assert(count > 0) { "Count must be > 0" }

        // Warming

        log.finer { "Warming up Java!" }
        repeat(warm) { execute("Warming loop $it") }

        // Actual testing

        var maxElapsed = LONG_MIN
        var minElapsed = LONG_MAX
        var totalElapsed: Long = 0

        var stats = Stats()

        repeat(count) {
            stats = execute("Performance test loop $it")
            minElapsed = minOf(stats.elapsed, minElapsed)
            maxElapsed = maxOf(stats.elapsed, maxElapsed)
            totalElapsed += stats.elapsed
        }

        val avgElapsed = totalElapsed / count
        val minKIPS = stats.executed.long / maxElapsed
        val maxKIPS = stats.executed.long / minElapsed
        val avgKIPS = stats.executed.long / avgElapsed

        log.info { "(min/avg/max) count=%2d exec=%,12d  KIPS: %,5d/%,5d/%,5d  time (ms): %,7d/%,7d/%,7d  total=%,d".format(
                count, stats.executed.long, minKIPS, avgKIPS, maxKIPS, minElapsed, avgElapsed, maxElapsed, totalElapsed) }
    }
}