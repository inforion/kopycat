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
package ru.inforion.lab403.kopycat.examples

import org.junit.Test
import ru.inforion.lab403.common.extensions.convertToString
import ru.inforion.lab403.common.extensions.times
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.OFF
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.auxiliary.PerformanceTester
import ru.inforion.lab403.kopycat.cores.base.common.ComponentTracer
import ru.inforion.lab403.kopycat.modules.cores.ARMv6MCore
import ru.inforion.lab403.kopycat.modules.examples.stm32f042_example
import ru.inforion.lab403.kopycat.modules.terminals.UartTerminal
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue


internal class stm32f042Tests {
    companion object {
        @Transient val log = logger(FINE)
    }

    init {
        log.info { "Working Directory: ${System.getProperty("user.dir")}" }

        UartTerminal.log.level = OFF
    }

    // TODO: https://youtrack.lab403.inforion.ru/issue/KC-1857

    @Test
    fun gpiox_led_test() {
        PerformanceTester(0x0800_11B4, 100_000_000) {
            stm32f042_example(null, "top", "example:gpiox_led")
        }.run(2, 1)
    }

    @Test
    fun gpiox_led_test_tracer() {
        PerformanceTester(0x0800_11B4, 100_000_000) {
            stm32f042_example(null, "top", "example:gpiox_led").also {
                val trc = ComponentTracer<ARMv6MCore>(it.stm32f042, "trc")
                it.stm32f042.buses.connect(trc.ports.trace, it.stm32f042.dbg.ports.trace)
            }
        }.run(2, 1)
    }

    @Test
    fun gpiox_registers_test() {
        PerformanceTester(0x0800_241E, 10_000_000) {
            stm32f042_example(null, "top", "example:gpiox_registers")
        }.run(2, 1)
    }

    @Test
    fun benchmark_qsort_test() {
        PerformanceTester(0x0800_1E62) {
            stm32f042_example(null, "top", "example:benchmark_qsort")
        }.run(5, 1)
    }

    private fun checkSocatOutput(path: String, expected: String) {
        val stream = File(path).inputStream()
        assertTrue("available=${stream.available()} expected=${expected.length }") {
            stream.available() >= expected.length
        }

        val result = ByteArray(expected.length).also { stream.read(it) }.convertToString()

        log.info { "Socat output: $result" }

        assertEquals(expected, result)
    }

    /**
     * {EN} Checks USART using polling data transfer is OK {EN}
     */
    @Test
    fun usart_poll_test() {
        val testStringPoll = "my-test-string?my-test-string\n"

        val tester = PerformanceTester(0x800_1D82, 1_000_000) {
            stm32f042_example(null, "top", "example:usart_poll", "socat:", "socat:")
        }.afterReset {
            it.sendStringIntoUART1(testStringPoll * 5)
        }.atAddressAlways(0x0800_1CF8) {
            log.info { "main -> Enter" }
        }.apply { run(5, 1) }

        checkSocatOutput(tester.top.term1.socat!!.pty1, testStringPoll)
        checkSocatOutput(tester.top.term2.socat!!.pty1, testStringPoll)
    }

    /**
     * {EN} Checks USART using DMA data transfer is OK {EN}
     */
    @Test
    fun usart_dma_test() {
        val testStringDMA = "very very long string\n"

        val tester = PerformanceTester(0x0800_23FC, 1_000_000) {
            stm32f042_example(null, "top", "example:usart_dma", "socat:", "socat:")
        }.atAddressOnce(0x0800_23EE) {
            it.sendStringIntoUART1(testStringDMA * 20)
        }.atAddressAlways(0x0800_0684) {
            log.severe { "Enter -> HAL_DMA_IRQHandler" }
        }.atAddressAlways(0x0800_07C6) {
            log.severe { "Exit -> HAL_DMA_IRQHandler" }
        }.apply { run(1, 0) }

        // in this test not echo just output to uart2
        checkSocatOutput(tester.top.term2.socat!!.pty1, testStringDMA)
    }

    /**
     * {EN} Checks USART working under FreeRTOS {EN}
     */
    @Test
    fun freertos_uart() {
        val tester = PerformanceTester(0x0800_3338, 15_000_000) {
            stm32f042_example(null, "top", "example:freertos_uart")
        }.atAddressAlways(0x0800_31FC) {
            log.severe { "Enter -> StarterTaskHandler" }
        }.atAddressAlways(0x0800_3250) {
            log.severe { "Enter -> UartTaskHandler" }
        }.atAddressAlways(0x0800_32A4) {
            log.severe { "Enter -> ProcessTaskHandler" }
        }.atAddressAlways(0x0800_32F8) {
            log.severe { "Enter -> WatchDogHandler" }
        }.run(5, 1)
    }
}