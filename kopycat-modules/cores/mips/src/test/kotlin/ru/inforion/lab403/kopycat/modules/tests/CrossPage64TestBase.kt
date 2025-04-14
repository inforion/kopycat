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

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.mips.config.Config0
import ru.inforion.lab403.kopycat.interfaces.*
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import ru.inforion.lab403.kopycat.modules.memory.RAM
import java.nio.ByteOrder
import kotlin.test.assertEquals

class CrossPage64TestBase(
    private val cpuEndian: ByteOrder,
    private val ramEndian: ByteOrder,
    private val busEndian: ByteOrder,
) : Module(null, "CrossPage64Test") {
    companion object {
        private const val PAGE1_VIRT: ULong = 0xFFFF_FFFF_FFFF_0000uL
        private const val PAGE2_VIRT: ULong = 0xFFFF_FFFF_FFFF_2000uL
        private const val PAGE3_VIRT: ULong = 0xFFFF_FFFF_FFFF_4000uL

        private const val PAGE2_PHYS: ULong = 0x4000uL
        private const val PAGE3_PHYS: ULong = 0x2000uL

        val PAGE1_VIRT_END = PAGE2_VIRT
        private const val PAGE1_PHYS_END = PAGE3_PHYS
    }

    val mips64 = MipsCore(
        this,
        "mips64",
        frequency = 800.MHz,
        1.0,
        1,
        0x00000000u,
        63,
        42,
        ArchitectureRevision = 2,
        Config0Preset = Config0().apply {
            MT = 1u
            AT = 2u
            BE = (cpuEndian === ByteOrder.BIG_ENDIAN).int
        }.data,
    )

    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem")
    }

    @DontAutoSerialize
    override val buses = Buses()

    private val ram = RAM(this, "ram", 0x0FFF_FFFF).also {
        it.endian = ramEndian
    }

    init {
        mips64.ports.mem.connect(buses.mem)
        ram.ports.mem.connect(buses.mem, 0u, busEndian)
        initializeAndResetAsTopInstance()
    }

    fun resetTest() {
        mips64.reset()
        mips64.cpu.pc = 0uL
        mips64.cop.regs.Status.FR = true
        mips64.cop.regs.Status.EXL = false

        // Page 1: 0xFFFF_FFFF_FFFF_0000uL -> 0x0000
        // Page 2: 0xFFFF_FFFF_FFFF_2000uL -> 0x4000
        // Page 3: 0xFFFF_FFFF_FFFF_4000uL -> 0x2000
        mips64.mmu.writeTlbEntry(0, 0u, 0xC000_03FF_FFFF_0000uL, 0x0Fu, 0x4Fu)
        mips64.mmu.writeTlbEntry(1, 0u, 0xC000_03FF_FFFF_2000uL, 0x010Fu, 0x014Fu)
        mips64.mmu.writeTlbEntry(2, 0u, 0xC000_03FF_FFFF_4000uL, 0x8Fu, 0xCFu)

        // Physical
        ram.ports.mem.store(PAGE1_PHYS_END - 8uL, ByteArray(8) { (it + 1).byte })
        ram.ports.mem.outq(PAGE3_PHYS, 0xFF_FF_FF_FF_FF_FF_FF_FFuL)
        ram.ports.mem.store(PAGE2_PHYS, ByteArray(8) { (it + 9).byte })
    }

    fun tlbTest() {
        assertEquals(0uL, mips64.mmu.translate(PAGE1_VIRT, 0, 4, AccessAction.LOAD))
        assertEquals(0x4000uL, mips64.mmu.translate(PAGE2_VIRT, 0, 4, AccessAction.LOAD))
        assertEquals(0x2000uL, mips64.mmu.translate(PAGE3_VIRT, 0, 4, AccessAction.LOAD))

        assertEquals("0102030405060708", mips64.load(PAGE1_VIRT_END - 8uL, 8).hexlify())
        assertEquals("FFFFFFFFFFFFFFFF", mips64.load(PAGE3_VIRT, 8).hexlify())
        assertEquals("090A0B0C0D0E0F10", mips64.load(PAGE2_VIRT, 8).hexlify())
    }

    fun ByteArray.slidingWindow(windowSize: Int) = sequence {
        for (i in 0..size) {
            val ending = minOf(i + windowSize, size)
            yield(
                copyOfRange(i, ending) + if (ending == size) {
                    ByteArray(windowSize - (ending - i))
                } else {
                    ByteArray(0)
                } to i
            )
        }
    }

    override fun toString() = "CrossPage64TestBase { cpuEndian: $cpuEndian, ramEndian: $ramEndian, busEndian: $busEndian }"
}
