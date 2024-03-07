/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.cores.mips.Microarchitecture
import ru.inforion.lab403.kopycat.interfaces.IValuable
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class CPRBank(val core: MipsCore) : ARegistersBankNG<MipsCore>(
        "Coprocessor General Purpose Registers", 32 * 8, core.cpu.BIT_DEPTH.bits, 8) { // or just 64

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

    open inner class COP0Register(
        name: String,
        id: Int,
        val sel: Int = 0,
        default: ULong = 0u,
        dtype: Datatype = DWORD
    ) : Register(name, index(id, sel), default, dtype)

    open inner class ReadOnly(name: String, id: Int, sel: Int, default: ULong = 0u) :
            COP0Register(name, id, sel, default) {
        override var value: ULong
            get() = super.value
            set(value) {
                log.warning { "[${core.cpu.pc.hex8}] Store data to $this = ${value.hex8} -> ignored" }
            }

        fun hardwarePreset(initValue: ULong) {
            super.value = initValue
            log.warning { "[${this.value}] Hardware preset data to RO reg $this = ${value.hex16}" }
        }
    }


    // TODO: refactor and test

    inner class INDEX : COP0Register("Index", 0) {
        /**
         * Probe Failure. Hardware writes this bit during execution
         * of the TLBP instruction to indicate whether a TLB
         * match occurred
         */
        var P by bitOf(31)

        /** TLB index */
        var index by fieldOf(30..0)
    }

    val Index = INDEX()

    inner class RANDOM : ReadOnly("Random", 1, 0) {
        private inline val lowerRandomBound get() = core.cop.regs.Wired.value
        private inline val upperRandomBound get() = (core.mmu.tlbEntries - 1).ulong_z

        override var value: ULong
            get() = random.ulong(lowerRandomBound, upperRandomBound + 1u)
            set(value) { super.value = value }

        override fun reset() {
            default = upperRandomBound
            super.reset()
        }
    }

    val Random = RANDOM()

    val EntryLo0 = COP0Register("EntryLo0", 2, dtype = core.cpu.BIT_DEPTH)
    val EntryLo1 = COP0Register("EntryLo1", 3, dtype = core.cpu.BIT_DEPTH)

    inner class CONTEXT : COP0Register("Context", 4, 0, dtype = core.cpu.BIT_DEPTH) {
        private val borderPTEBase = if (core.is64bit) 63 else 23
        var PTEBase by fieldOf(borderPTEBase..23)
        var BadVPN2 by fieldOf(22..4)
    }

    val Context = CONTEXT()

    val UserLocal = COP0Register("UserLocal", 4, 2, dtype = core.cpu.BIT_DEPTH)

    inner class MMIDRegister : COP0Register("MMID", 4, 5, dtype = core.cpu.BIT_DEPTH) {
        var mmid by fieldOf(15..0)
    }

    val MMID = MMIDRegister()

    val PageMask = COP0Register("PageMask", 5, 0) // WARNING: Big Pages feature NOT implemented

    inner class PAGEGRAIN : COP0Register("PageGrain", 5, 1) {
        /**
         * Enables unique exception codes for the Read-Inhibit and
         * Execute-Inhibit exceptions
         */
        var IEC by bitOf(27)

        /** Enables support for large physical addresses */
        var ELPA by bitOf(29)
    }

    val PageGrain = PAGEGRAIN()

    class SEGCTL_CFG(override var data: ULong) : IValuable {
        /** Physical address bits for Segment, for use when unmapped */
        var PA by field(15..9)
        /** Access control mode */
        var AM by field(6..4)
        /**
         * Error condition behavior. Segment becomes unmapped
         * and uncached when Status_ERL=1
         */
        var EN by bit(3)
        /** Cache coherency attribute, for use when unmapped */
        var C by field(2..0)
    }

    open inner class SEGCTL0(n: Int, default: ULong = 0x200010uL) : COP0Register(
        "SegCtl$n",
        5,
        2 + n,
        default = default,
    ) {
        val cfgLo get() = SEGCTL_CFG(value[15..0])
        val cfgHi get() = SEGCTL_CFG(value[31..16])
    }

    inner class SEGCTL1 : SEGCTL0(1, 0x030002uL) {
        /** xkphys region access mode */
        val XAM by fieldOf(61 .. 59)
    }

    inner class SEGCTL2 : SEGCTL0(2, 0x3a043auL) {
        /**
         * xkphys region access mode enable. Each bit of XR[0..7]
         * defines access mode enable for the corresponding region
         * of the xkphys segment.
         */
        val XR by fieldOf(63 .. 56)
    }

    val SegCtl0 = SEGCTL0(0)
    val SegCtl1 = SEGCTL1()
    val SegCtl2 = SEGCTL2()
    val Wired = COP0Register("Wired", 6)
    val HWREna = COP0Register("HWREna", 7)

    val BadVAddr = COP0Register("BadVAddr", 8, dtype = core.cpu.BIT_DEPTH)
    val Count = COP0Register("Count", 9)

    inner class CVMCTL : COP0Register("CvmCtl", 9, 7) {
        var FUSE_START by bitOf(31)
        var REPUN by bitOf(14)
        var USEUN by bitOf(12)
        var LE by bitOf(1)
        var USELY by bitOf(0)

        override fun reset() {
            super.reset()
            LE = core.Config0Preset[15].untruth // !Config0.BE
            FUSE_START = true
        }

        override var value: ULong
            get() = super.value
            set(value) {
                super.value = value set 31
                Config0.BE = !LE
            }
    }

    val CvmCtl = if (core.microarchitecture == Microarchitecture.cnMips) {
        CVMCTL()
    } else {
        null
    }

    inner class ENTRY_HI : COP0Register("EntryHi", 10, dtype = core.cpu.BIT_DEPTH) {
        private val borderVPN2 = if (core.is64bit) core.SEGBITS - 1 else 31

        var VPN2 by fieldOf(borderVPN2 .. 13)
        var VPN2X by fieldOf(12..11)
        var EHINV by bitOf(10)

        /** Value of ASID. Depends on the current value of [CONFIG4.ae]. */
        var ASID
            get() = if (core.cop.regs.Config4.ae) {
                value[9..0]
            } else {
                value[7..0]
            }
            set(newValue) {
                value = value.insert(newValue, 9..0)
            }

        override var value: ULong
            get() = super.value
            set(value) {
                when  {
                    core.is32bit -> super.value = value
                    else -> {
                        super.value = super.value
                            .insert(value[63..62], 63..62)
                            // bits 61..40 = Fill -> reserved
                            .insert(value[borderVPN2..0], borderVPN2..0)
                    }
                }
            }
    }

    val EntryHi = ENTRY_HI()

    inner class COMPARE : COP0Register("Compare", 11) {
        override var value: ULong
            get() = super.value
            set(value) {
                core.cop.lowerCountCompareIrq()
                super.value = value[31..0]
            }
    }

    val Compare = COMPARE()

    inner class CVMMEMCTL : COP0Register("CvmMemCtl", 11, 7) {
        /** Size of local memory in cache blocks  */
        var LMEMSZ by fieldOf(5..0)
        /** If set, CVMSEG is available for loads/stores in user mode */
        var CVMSEGENAU by bitOf(6)
        /** If set, CVMSEG is available for loads/stores in supervisor mode */
        var CVMSEGENAS by bitOf(7)
        /** If set, CVMSEG is available for loads/stores in kernel/debug mode */
        var CVMSEGENAK by bitOf(8)

        override var value
            get() = super.value
            set(value) {
                super.value = value clr 63..58
            }
    }

    val CvmMemCtl = if (core.microarchitecture == Microarchitecture.cnMips) {
        CVMMEMCTL()
    } else {
        null
    }

    enum class ProcessorMode(val ksu: Int) {
        Kernel(0),
        Supervisor(1),
        User(2);

        companion object {
            fun fromKSU(status: STATUS) = values().find { it.ksu == status.KSU.int }
        }
    }

    // TODO: Status FPU register (FR) must depend on fpuDtype or vise versa
    /**
     * See MIPS PRA, p. 9.33, table 949, page 214
     */
    inner class STATUS : COP0Register("Status", 12, 0, default = 0x400004uL) {
        var IE by bitOf(0)
        var EXL by bitOf(1)
        var ERL by bitOf(2)
        var R0 by bitOf(3)
        var UM by bitOf(4)
        var KSU by fieldOf(4..3)
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
        /**
         * For Interrupt Compatibility and Vectored Interrupt modes,
         * this field specifies the IP number to which the Timer Interrupt
         * request is merged, and allows software to determine
         * whether to consider CauseTI for a potential interrupt.
         *
         * |Encoding|IP bit|Hardware Interrupt Source|
         * |-|-|-|
         * |2|2|HW0|
         * |3|3|HW1|
         * |4|4|HW2|
         * |5|5|HW3|
         * |6|6|HW4|
         * |7|7|HW5|
         */
        var IPTI by fieldOf(31..29)
        var IPPCI by fieldOf(28..26)
        var IPFDC by fieldOf(25..23)
        var MCU_ASE by fieldOf(22..14)
        var VS by fieldOf(9..5)
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

        /**
         * Indicates an interrupt is pending. If EIC interrupt
         * mode is enabled (Config3_VEIC = 1), these bits take on a
         * different meaning and are interpreted as the RIPL field.
         *
         * |Bit|Name|Meaning
         * |-|-|-|
         * |15|IP7|Hardware interrupt 5
         * |14|IP6|Hardware interrupt 4
         * |13|IP5|Hardware interrupt 3
         * |12|IP4|Hardware interrupt 2
         * |11|IP3|Hardware interrupt 1
         * |10|IP2|Hardware interrupt 0
         */
        var IP7_2 by fieldOf(15..10)

        /**
         * Requested Interrupt Priority Level. If EIC interrupt mode is
         * enabled (Config3_VEIC = 1), this field is the encoded
         * (0..63) value of the requested interrupt. A value of zero
         * indicates that no interrupt is requested.
         */
        var RIPL by fieldOf(15..10)

        /** Fast Debug Channel Interrupt */
        var FDCI by bitOf(21)

        /**
         * Indicates that a watch exception was deferred because
         * Status_EXL or Status_ERL were a one at the time the watch
         * exception was detected
         */
        var WP by bitOf(22)

        /**
         * Indicates whether an interrupt exception uses the general
         * exception vector or a special interrupt vector:
         *
         * |Encoding|Meaning|
         * |-|-|
         * |0|Use the general exception vector (0x180)|
         * |1|Use the special interrupt vector (0x200)|
         */
        var IV by bitOf(23)   // Use the general exception vector or a special interrupt vector

        /** Performance Counter Interrupt */
        var PCI by bitOf(26)

        /** Disable Count register */
        var DC by bitOf(27)

        /** Coprocessor unit number referenced when a Coprocessor Unusable exception is taken */
        var CE by fieldOf(29..28)

        /**
         * Timer Interrupt. In an implementation of Release 2 of the
         * Architecture, this bit denotes whether a timer interrupt is
         * pending (analogous to the IP bits for other interrupt
         * types).
         */
        var TI by bitOf(30)

        /** Indicates whether the last exception taken occurred in a branch delay slot */
        var BD by bitOf(31)
    }

    val Cause = CAUSE()

    val EPC = COP0Register("EPC", 14, dtype = core.cpu.BIT_DEPTH)

    val PRId = COP0Register("PRId", 15, 0, core.PRId)

    /* The fixed value of EBase31..30 forces the base to be in kseg0 or kseg1 */
    inner class EBASE : COP0Register("EBase", 15, 1, 0x8000_0000u) {
        var CPUNum by fieldOf(9..0)
        var ExceptionBase by fieldOf(29..12)

        override var value: ULong
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
        var BE by bitOf(15)
        var K0 by fieldOf(2..0)

        override var value: ULong
            get() = super.value
            set(value) {
                // we can't use these field inside register, because it will lead to recursion
                super.value = super.value
                        // other field just ignored for this register
                        .insert(value[30..28], 30..28)
                        .insert(value[27..25], 27..25)
                        .insert(value[2..0], 2..0)
            }

        fun hardwarePreset(initValue: ULong) {
            super.value = initValue
            log.warning { "[${this.value}] Store data to $this = ${value.hex16}" }
        }
    }

    val Config0 = CONFIG0()

    val Config1 = ReadOnly("Config1", 16, 1, core.Config1Preset)
    val Config2 = ReadOnly("Config2", 16, 2, core.Config2Preset)

    inner class CONFIG3 : ReadOnly("Config3", 16, 3, core.Config3Preset) {
        val ULRI by bitOf(13) // UserLocal register implemented
        val VEIC by bitOf(6)  // Support for an external interrupt controller is implemented.
        val VInt by bitOf(5)  // Vectored interrupts implemented

        /** Large Physical Address support is implemented, and the PageGrain register exists */
        val LPA by bitOf(7)
    }

    val Config3 = CONFIG3()

    inner class CONFIG4 : ReadOnly("Config4", 16, 4, core.Config4Preset) {
        /** If this bit is set, then EntryHI_ASID is extended to 10 bits */
        var ae by bitOf(28)
    }

    val Config4 = CONFIG4()

    inner class CONFIG5 : ReadOnly("Config5", 16, 5) {
        /** Indicates whether the ASID or MMID is used for FTLB translations */
        var mi by bitOf(17)
    }

    val Config5 = CONFIG5()
//    val Config6 = ReadOnly("Config6", 16, 6)

    val Config7 = ReadOnly("Config7", 16, 7)        // wtf

    val LLAddr = COP0Register("LLAddr", 17, 0, dtype = core.cpu.BIT_DEPTH)

    inner class WATCHLO0: COP0Register("WatchLo0", 18, 0, dtype = core.cpu.BIT_DEPTH) {
        override var value: ULong
            get() = super.value
            set(value) {
                // we can't use these field inside register, because it will lead to recursion
                super.value = super.value
                    // WARNING! Implementation-dependent. p 307 PRA
                    // 2-0 bits just ignored for this register
                    .insert(value[63..3], 63..3)

            }
    }

    val WatchLo0 = WATCHLO0()

    inner class WATCHHI0: COP0Register("WatchHi0", 19, 0) {
        // TODO: set by proc i/r/w bits if address in watch_hi/watch_lo was accessed
        // TODO: hardware preset
        override var value: ULong
            get() = super.value
            set(value) {
                super.value = super.value
                    // M=[31] -- if other pair of watch regs is implemented
                    // .insert(value[31], 31)
                    .insert(value[30], 30)
                    // 25:25 depends on config4
                    .insert(value[23..16], 23..16)
                    .insert(value[11..3], 11..3)
                if (value[2] == 1uL) super.value.insert(0, 2)
                if (value[1] == 1uL) super.value.insert(0, 1)
                if (value[0] == 1uL) super.value.insert(0, 0)
            }
        fun hardwarePreset(initValue: ULong) {
            super.value = initValue
            log.warning { "[${this.value}] Hardware preset data to WatchHi0 reg $this = ${value.hex16}" }
        }
    }

    val WatchHi0 = WATCHHI0()

    val WatchLo1 = COP0Register("WatchLo1", 18, 1, dtype = core.cpu.BIT_DEPTH)
    val WatchHi1 = COP0Register("WatchHi1", 19, 1)
    val WatchLo2 = COP0Register("WatchLo2", 18, 2, dtype = core.cpu.BIT_DEPTH)
    val WatchHi2 = COP0Register("WatchHi2", 19, 2)
    val WatchLo3 = COP0Register("WatchLo3", 18, 3, dtype = core.cpu.BIT_DEPTH)
    val WatchHi3 = COP0Register("WatchHi3", 19, 3)

    /** 64-bit PTE entry pointer */
    inner class XCONTEXT : COP0Register("XContext", 20, dtype = core.cpu.BIT_DEPTH) {
        var PTEBase by fieldOf(63 .. core.SEGBITS - 7)
        /**
         * Region field: contains bits 63..62 of the virtual address
         *
         * |Encoding|Meaning|
         * |-|-|
         * |0b00|xuseg|
         * |0b01|xsseg: supervisor address region. If Supervisor Mode is not implemented, this encoding is reserved|
         * |0b10|Reserved|
         * |0b11|xkseg|
         */
        var R by fieldOf(core.SEGBITS - 8 .. core.SEGBITS - 9)
        /**
         * The Bad Virtual Page Number/2 field is written by
         * hardware on a miss. It contains bits VA[SEGBITS - 1..13] of
         * the virtual address that missed.
         */
        var BadVPN2 by fieldOf(core.SEGBITS - 10 .. 4)
        /** Must be written as zero; returns zero on read. */
        var zero by fieldOf(3 .. 0)
    }

    val XContext = XCONTEXT() // I6500 Multiprocessing System Programmer’s Guide

    val Debug = COP0Register("Debug", 23)           // TLDR, see EJTAG Specification

    val DEPC0 = COP0Register("DEPC0", 24, 0, dtype = core.cpu.BIT_DEPTH)    // EJTAG Specification
    val DEPC6 = COP0Register("DEPC6", 24, 6, dtype = core.cpu.BIT_DEPTH)

    val PerfCnt = COP0Register("PerfCnt", 25)
    val ErrCtl = COP0Register("ErrCtl", 26)

    val CacheErr0 = COP0Register("CacheErr0", 27, 0)
    val CacheErr1 = COP0Register("CacheErr1", 27, 1)
    val CacheErr2 = COP0Register("CacheErr2", 27, 2)
    val CacheErr3 = COP0Register("CacheErr3", 27, 3)

    val TagLo0 = COP0Register("TagLo0", 28, 0, dtype = core.cpu.BIT_DEPTH)
    val TagHi0 = COP0Register("TagHi0", 29, 0)

    val TagLo2 = COP0Register("TagLo2", 28, 2, dtype = core.cpu.BIT_DEPTH)
    val TagHi2 = COP0Register("TagHi2", 29, 2)

    val TagLo4 = COP0Register("TagLo4", 28, 4, dtype = core.cpu.BIT_DEPTH)
    val TagHi4 = COP0Register("TagHi4", 29, 4)

    val DataLo1 = COP0Register("DataLo1", 28, 1)
    val DataHi1 = COP0Register("DataHi1", 29, 1)

    val DataLo3 = COP0Register("DataLo3", 28, 3)
    val DataHi3 = COP0Register("DataHi3", 29, 3)

    val ErrorEPC = COP0Register("ErrorEPC", 30, dtype = core.cpu.BIT_DEPTH)

    val DESAVE = COP0Register("DESAVE", 31)
}
