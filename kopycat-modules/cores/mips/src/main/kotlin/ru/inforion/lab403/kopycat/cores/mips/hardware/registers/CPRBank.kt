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
@file:Suppress("PropertyName", "unused")

package ru.inforion.lab403.kopycat.cores.mips.hardware.registers

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class CPRBank(val core: MipsCore) : ARegistersBankNG<MipsCore>(
        "Coprocessor General Purpose Registers", 32 * 8, 32, 8) {

    companion object {
        /**
         * {EN}
         * Generates index of register using the following rule:
         * id   0  1  2  3   <- sel
         *  0   0 32 64 96
         *  1   1 33 65 97
         *  2   2 34 66 98 <- index
         *  3   3 35 67 99
         * {EN}
         */
        @Suppress("NOTHING_TO_INLINE")
        inline fun index(id: Int, sel: Int) = id or (sel shl 5)
    }

    operator fun get(id: Int, sel: Int) = get(index(id, sel))

    open inner class COP0Register(name: String, id: Int, val sel: Int = 0, default: Long = 0) :
            Register(name, index(id, sel), default)

    open inner class ReadOnly(name: String, id: Int, sel: Int, default: Long = 0) :
            COP0Register(name, id, sel, default) {
        override var value: Long
            get() = super.value
            set(value) {
                log.warning { "[${core.cpu.pc.hex8}] Store data to $this = ${value.hex8} -> ignored" }
            }
    }

    val Index = COP0Register("Index", 0)

    inner class RANDOM : ReadOnly("Random", 1, 0) {
        private inline val lowerRandomBound get() = core.cop.regs.Wired.value
        private inline val upperRandomBound get() = (core.mmu.tlbEntries - 1).asLong

        override var value: Long
            get() = random.long(lowerRandomBound, upperRandomBound + 1)
            set(value) { super.value = value }

        override fun reset() {
            default = upperRandomBound
            super.reset()
        }
    }

    val Random = RANDOM()

    val EntryLo0 = COP0Register("EntryLo0", 2)
    val EntryLo1 = COP0Register("EntryLo1", 3)

    inner class CONTEXT : COP0Register("Context", 4, 0) {
        var PTEBase by fieldOf(31..23)
        var BadVPN2 by fieldOf(22..4)
    }

    val Context = CONTEXT()

    val UserLocal = COP0Register("UserLocal", 4, 2)
    val PageMask = COP0Register("PageMask", 5)
    val Wired = COP0Register("Wired", 6)
    val HWREna = COP0Register("HWREna", 7)
    val BadVAddr = COP0Register("BadVAddr", 8)
    val Count = COP0Register("Count", 9)

    inner class ENTRY_HI : COP0Register("EntryHi", 10) {
        var VPN2 by fieldOf(31..13)
        var VPN2X by fieldOf(12..11)
        var EHINV by bitOf(10)
        var ASIDX by fieldOf(9..8)
        var ASID by fieldOf(7..0)
    }

    val EntryHi = ENTRY_HI()

    inner class COMPARE : COP0Register("Compare", 11) {
        override var value: Long
            get() = super.value

            set(value) {
                core.cop.clearCountCompareTimerBits()
                super.value = value
            }
    }

    val Compare = COMPARE()

    inner class STATUS : COP0Register("Status", 12, 0) {
        var IE by bitOf(0)
        var EXL by bitOf(1)
        var ERL by bitOf(2)
        var R0 by bitOf(3)
        var UM by bitOf(4)
        var UX by bitOf(5)
        var SX by bitOf(6)
        var KX by bitOf(7)

        var IM0 by bitOf(8)
        var IM1 by bitOf(9)
        var IM2 by bitOf(10)
        var IM3 by bitOf(11)
        var IM4 by bitOf(12)
        var IM5 by bitOf(13)
        var IM6 by bitOf(14)
        var IM7 by bitOf(15)

        var IM7_0 by fieldOf(18..8)
        var IM7_2 by fieldOf(18..10)

        var NMI by bitOf(19)
        var SR by bitOf(20)
        var TS by bitOf(21)
        var BEV by bitOf(22)
        var PX by bitOf(23)
        var MX by bitOf(24)
        var RE by bitOf(25)
        var FR by bitOf(26)
        var RP by bitOf(27)
        var CU0 by bitOf(28)
        var CU1 by bitOf(29)
        var CU2 by bitOf(30)
        var CU3 by bitOf(31)
    }

    val Status = STATUS()

    inner class INTCTL : COP0Register("IntCtl", 12, 1, core.IntCtlPreset) {
        var IPTI by fieldOf(31..29)
        var IPPCI by fieldOf(28..26)
        var IPFDC by fieldOf(25..23)
        var MCU_ASE by fieldOf(22..14)
        var VS by fieldOf(9..5)

        override var value: Long
            get() = super.value
            set(value) {
                VS = value
            }
    }

    val IntCtl = INTCTL() // IMPLSPEC0

    inner class SRSCTL : COP0Register("SRSCtl", 12, 2) {
        var HSS by fieldOf(29..26)
        var EICSS by fieldOf(21..18)
        var ESS by fieldOf(15..12)
        var PSS by fieldOf(9..6)
        var CSS by fieldOf(3..0)
    }

    val SRSCtl = SRSCTL() // IMPLPIC32

    inner class SRSMAP : COP0Register("SRSMap", 12, 3) {
        var SSV7 by fieldOf(31..28)
        var SSV6 by fieldOf(27..24)
        var SSV5 by fieldOf(23..20)
        var SSV4 by fieldOf(19..16)
        var SSV3 by fieldOf(15..12)
        var SSV2 by fieldOf(11..8)
        var SSV1 by fieldOf(7..4)
        var SSV0 by fieldOf(3..0)
    }

    val SRSMap = SRSMAP() // IMPLSPEC1

    inner class CAUSE : COP0Register("Cause", 13) {
        var EXC by fieldOf(6..2)

        var IP0 by bitOf(8)   // Request software interrupt 0
        var IP1 by bitOf(9)   // Request software interrupt 1
        var IP2 by bitOf(10)  // Hardware interrupt 0
        var IP3 by bitOf(11)  // Hardware interrupt 1
        var IP4 by bitOf(12)  // Hardware interrupt 2
        var IP5 by bitOf(13)  // Hardware interrupt 3
        var IP6 by bitOf(14)  // Hardware interrupt 4
        var IP7 by bitOf(15)  // Hardware interrupt 5, timer or performance counter interrupt

        var IP7_0 by fieldOf(15..8)
        var IP7_2 by fieldOf(15..10)

        var WP by bitOf(22)
        var IV by bitOf(23)   // Use the general exception vector or a special interrupt vector

        var CE by fieldOf(29..28)

        var TI by bitOf(30)
        var BD by bitOf(31)
    }

    val Cause = CAUSE()

    val EPC = COP0Register("EPC", 14)

    val PRId = COP0Register("PRId", 15, 0, core.PRId)

    /* The fixed value of EBase31..30 forces the base to be in kseg0 or kseg1 */
    inner class EBASE : COP0Register("EBase", 15, 1, 0x8000_0000) {
        var CPUNum by fieldOf(9..0)
        var ExceptionBase by fieldOf(29..12)

        override var value: Long
            get() = super.value
            set(value) {
                // we can't use these field inside register, because it will lead to recursion
                super.value = super.value
                        // other field just ignored for this register
                        .insert(value[29..12], 29..12)
            }
    }

    val EBase = EBASE()

    inner class CONFIG0 : COP0Register("Config0", 16, 0, core.Config0Preset) {
        var K23 by fieldOf(30..28)
        var KU by fieldOf(27..25)
        var K0 by fieldOf(2..0)

        override var value: Long
            get() = super.value
            set(value) {
                // we can't use these field inside register, because it will lead to recursion
                super.value = super.value
                        // other field just ignored for this register
                        .insert(value[30..28], 30..28)
                        .insert(value[27..25], 27..25)
                        .insert(value[2..0], 2..0)
            }
    }

    val Config0 = CONFIG0()

    val Config1 = ReadOnly("Config1", 16, 1, core.Config1Preset)
    val Config2 = ReadOnly("Config2", 16, 2, core.Config2Preset)

    inner class CONFIG3 : ReadOnly("Config3", 16, 3, core.Config3Preset) {
        val ULRI by bitOf(13) // UserLocal register implemented
        val VEIC by bitOf(6)  // Support for an external interrupt controller is implemented.
        val VInt by bitOf(5)  // Vectored interrupts implemented
    }

    val Config3 = CONFIG3()

//    val Config4 = ReadOnly("Config4", 16, 4)
//    val Config5 = ReadOnly("Config5", 16, 5)
//    val Config6 = ReadOnly("Config6", 16, 6)

    val Config7 = ReadOnly("Config7", 16, 7)

    val LLAddr = COP0Register("LLAddr", 17)

    val WatchLo0 = COP0Register("WatchLo0", 18, 0)
    val WatchHi0 = COP0Register("WatchHi0", 19, 0)
    val WatchLo1 = COP0Register("WatchLo1", 18, 1)
    val WatchHi1 = COP0Register("WatchHi1", 19, 1)
    val WatchLo2 = COP0Register("WatchLo2", 18, 2)
    val WatchHi2 = COP0Register("WatchHi2", 19, 2)
    val WatchLo3 = COP0Register("WatchLo3", 18, 3)
    val WatchHi3 = COP0Register("WatchHi3", 19, 3)

    val XContext = COP0Register("XContext", 20)  // I6500 Multiprocessing System Programmer’s Guide

    val Debug = COP0Register("Debug", 23)

    val DEPC0 = COP0Register("DEPC0", 24, 0)
    val DEPC6 = COP0Register("DEPC6", 24, 6)

    val PerfCnt = COP0Register("PerfCnt", 25)
    val ErrCtl = COP0Register("ErrCtl", 26)

    val CacheErr0 = COP0Register("CacheErr0", 27, 0)
    val CacheErr1 = COP0Register("CacheErr1", 27, 1)
    val CacheErr2 = COP0Register("CacheErr2", 27, 2)
    val CacheErr3 = COP0Register("CacheErr3", 27, 3)

    val TagLo0 = COP0Register("TagLo0", 28, 0)
    val TagHi0 = COP0Register("TagHi0", 29, 0)

    val TagLo2 = COP0Register("TagLo2", 28, 2)
    val TagHi2 = COP0Register("TagHi2", 29, 2)

    val TagLo4 = COP0Register("TagLo4", 28, 4)
    val TagHi4 = COP0Register("TagHi4", 29, 4)

    val DataLo1 = COP0Register("DataLo1", 28, 1)
    val DataHi1 = COP0Register("DataHi1", 29, 1)

    val DataLo3 = COP0Register("DataLo3", 28, 3)
    val DataHi3 = COP0Register("DataHi3", 29, 3)

    val ErrorEPC = COP0Register("ErrorEPC", 30)

    val DESAVE = COP0Register("DESAVE", 31)
}
