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
package ru.inforion.lab403.kopycat.modules.tests

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.interfaces.inl
import ru.inforion.lab403.kopycat.interfaces.inq
import ru.inforion.lab403.kopycat.interfaces.outq
import ru.inforion.lab403.kopycat.modules.tests.CrossPage64TestBase.Companion.PAGE1_VIRT_END
import java.nio.ByteOrder
import java.util.stream.Stream
import kotlin.test.assertEquals

class CrossPage64Test {
    companion object {
        private val ramBE = CrossPage64TestBase(
            cpuEndian = ByteOrder.BIG_ENDIAN,
            ramEndian = ByteOrder.BIG_ENDIAN,
            busEndian = ByteOrder.LITTLE_ENDIAN,
        )
        private val busBE = CrossPage64TestBase(
            cpuEndian = ByteOrder.BIG_ENDIAN,
            ramEndian = ByteOrder.LITTLE_ENDIAN,
            busEndian = ByteOrder.BIG_ENDIAN,
        )
        private val allLE = CrossPage64TestBase(
            cpuEndian = ByteOrder.LITTLE_ENDIAN,
            ramEndian = ByteOrder.LITTLE_ENDIAN,
            busEndian = ByteOrder.LITTLE_ENDIAN,
        )
        @JvmStatic fun testBaseProvider(): Stream<Pair<CrossPage64TestBase, ByteOrder>> = Stream.of(
            ramBE to ByteOrder.BIG_ENDIAN,
            busBE to ByteOrder.BIG_ENDIAN,
            allLE to ByteOrder.LITTLE_ENDIAN,
        )
    }

    @BeforeEach fun resetTest() {
        ramBE.resetTest()
        busBE.resetTest()
        allLE.resetTest()
    }

    // 01 02 03 04 05 06 07 08 | 09 0A 0B 0C 0D 0E 0F 10
    //          PAGE1_VIRT_END ^
    @ParameterizedTest
    @MethodSource("testBaseProvider")
    fun crosspageRead(arg: Pair<CrossPage64TestBase, ByteOrder>) = arg.first.run {
        tlbTest()
        if (arg.second === ByteOrder.BIG_ENDIAN) {
            assertEquals(0x05_06_07_08uL.hex8, mips64.inl(PAGE1_VIRT_END - 4uL).hex8)
            assertEquals(0x05_06_07_08_09_0A_0B_0CuL.hex16, mips64.inq(PAGE1_VIRT_END - 4uL).hex16)
        } else {
            assertEquals(0x08_07_06_05uL.hex8, mips64.inl(PAGE1_VIRT_END - 4uL).hex8)
            assertEquals(0x0C_0B_0A_09_08_07_06_05uL.hex16, mips64.inq(PAGE1_VIRT_END - 4uL).hex16)
        }
    }

    @ParameterizedTest
    @MethodSource("testBaseProvider")
    fun crosspageFetch(arg: Pair<CrossPage64TestBase, ByteOrder>) = arg.first.run {
        tlbTest()
        if (arg.second === ByteOrder.BIG_ENDIAN) {
            assertEquals(0x05_06_07_08uL.hex8, mips64.fetch(PAGE1_VIRT_END - 4uL, 0, 4).hex8)
            assertEquals(0x05_06_07_08_09_0A_0B_0CuL.hex16, mips64.fetch(PAGE1_VIRT_END - 4uL, 0, 8).hex16)
        } else {
            assertEquals(0x08_07_06_05uL.hex8, mips64.fetch(PAGE1_VIRT_END - 4uL, 0, 4).hex8)
            assertEquals(0x0C_0B_0A_09_08_07_06_05uL.hex16, mips64.fetch(PAGE1_VIRT_END - 4uL, 0, 8).hex16)
        }
    }

    @ParameterizedTest
    @MethodSource("testBaseProvider")
    fun crosspageWrite(arg: Pair<CrossPage64TestBase, ByteOrder>) = arg.first.run {
        tlbTest()
        mips64.outq(PAGE1_VIRT_END - 4uL, 0x05_06_07_08_09_0A_0B_0CuL)
        assertEquals(0x05_06_07_08_09_0A_0B_0CuL.hex16, mips64.inq(PAGE1_VIRT_END - 4uL).hex16)
        if (arg.second === ByteOrder.BIG_ENDIAN) {
            assertEquals("05060708090A0B0C", mips64.load(PAGE1_VIRT_END - 4uL, 8).hexlify())
        } else {
            assertEquals("0C0B0A0908070605", mips64.load(PAGE1_VIRT_END - 4uL, 8).hexlify())
        }
    }

    @ParameterizedTest
    @MethodSource("testBaseProvider")
    fun crosspageLoop(arg: Pair<CrossPage64TestBase, ByteOrder>) = arg.first.run {
        tlbTest()
        val expectedData = "0102030405060708090A0B0C0D0E0F10".unhexlify()

        setOf(2, 4, 8).forEach { windowSize ->
            expectedData.slidingWindow(windowSize).forEach { (window, i) ->
                val ea = PAGE1_VIRT_END - 8u + i
                assertEquals(
                    window.hexlify(),
                    mips64
                        .read(ea, 0, windowSize)
                        .pack(windowSize, arg.second)
                        .hexlify(),
                )
                mips64.write(ea, 0, windowSize, window.getUInt(0, windowSize, arg.second))
                assertEquals(
                    window.hexlify(),
                    mips64
                        .fetch(ea, 0, windowSize)
                        .pack(windowSize, arg.second)
                        .hexlify(),
                )
            }
        }
    }
}
