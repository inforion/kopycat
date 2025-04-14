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

import org.junit.jupiter.api.Test
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.proposal.swapIfBE
import ru.inforion.lab403.kopycat.cores.base.Port
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.exceptions.CrossPageAccessException
import ru.inforion.lab403.kopycat.modules.cores.device.TestTopDevice
import java.nio.ByteOrder
import kotlin.test.assertEquals
import kotlin.test.fail

private class CrosspageTest(private val endian: ByteOrder) : Module(null, "Crosspage test") {
    inner class Ports : ModulePorts(this) {
        val physical = Port("physical")
        val virtual = Port("virtual")
    }

    override val ports = Ports()
    override val buses = ModuleBuses(this)

    init {
        TestTopDevice(this, "DummyTop")

        val entries = setOf(
            0uL until 0x100uL to 0uL,
            0x100uL until 0x200uL to 0x200uL,
            0x200uL until 0x300uL to 0x100uL,
        )

        val translator = object : AddressTranslator(this@CrosspageTest, "translator") {
            override fun translate(ea: ULong, ss: Int, size: Int, LorS: AccessAction) = entries.find {
                it.first.contains(ea)
            }?.let {
                val entryStart = it.first.first
                val entryEnd = it.first.last
                val entryPhysStart = it.second

                if (ea > entryEnd - size.ulong_z + 1u) {
                    throw CrossPageAccessException(0uL, ea, 0xFFuL.inv(), order = endian)
                } else {
                    ea - entryStart + entryPhysStart
                }
            } ?: throw RuntimeException()
        }

        buses.connect(translator.ports.outp, ports.physical)
        buses.connect(translator.ports.inp, ports.virtual)
    }

    fun innerTest() {
        val buf = "0102030405060708".unhexlify()
        object : Area(ports.physical, 0uL, 0x2FFuL, "area") {
            private var lastBeforeWrite = 0uL

            override fun read(ea: ULong, ss: Int, size: Int): ULong {
                return when (ea) {
                    in 0xFCuL until 0x100uL -> buf.getUInt((ea - 0xFCuL).int, size, endian)
                    in 0x200uL until 0x204uL -> buf.getUInt((ea - 0x200uL + 4u).int, size, endian)
                    else -> fail("You're not supposed to be here")
                }
            }

            override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
                assertEquals(lastBeforeWrite, value)

                when (ea) {
                    0xFCuL -> buf.putUInt(0, value, size, endian)
                    0x200uL -> buf.putUInt(4, value, size, endian)
                    else -> fail("You're not supposed to be here")
                }
            }

            override fun beforeWrite(from: Port, ea: ULong, size: Int, value: ULong): Boolean {
                lastBeforeWrite = value
                return super.beforeWrite(from, ea, size, value)
            }
        }

        initializeAndResetAsTopInstance()

        assertEquals(0x04_03_02_01uL.swapIfBE(endian, 4), ports.virtual.read(0xFCuL, 0, 4))
        assertEquals(0x08_07_06_05uL.swapIfBE(endian, 4), ports.virtual.read(0x100uL, 0, 4))
        assertEquals(0x08_07_06_05_04_03_02_01uL.swapIfBE(endian, 8), ports.virtual.read(0xFCuL, 0, 8))
        ports.virtual.write(0xFCuL, 0, 8, 0x10_0F_0E_0D_0C_0B_0A_09uL)
        assertEquals(0x10_0F_0E_0D_0C_0B_0A_09uL, ports.virtual.read(0xFCuL, 0, 8))
    }
}

class CrosspageBETest {
    @Test fun test() = CrosspageTest(ByteOrder.BIG_ENDIAN).innerTest()
}

class CrosspageLETest {
    @Test fun test() = CrosspageTest(ByteOrder.LITTLE_ENDIAN).innerTest()
}
