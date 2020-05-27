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
package ru.inforion.lab403.kopycat.modules.p2020

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.AddressTranslator
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.modules.BUS32


 
class DDRController(parent: Module,
                    name: String
/*                    ddr0Size: Long = 0,
                    ddr1Size: Long = 0,
                    ddr2Size: Long = 0,
                    ddr3Size: Long = 0*/) : Module(parent, name) {

    inner class Ports : ModulePorts(this) {
        val inp: Translator
            get() = filter.ports.inp
        val outp: Master
            get() = filter.ports.outp
        val ctrl = Slave("ctrl", BUS32)
    }

    override val ports = Ports()

    /*inner class Buses : ModuleBuses(this) {
//        val intern = Bus("intern", BUS32)
//        val mem = Bus("mem", BUS32)
    }

    override val buses = Buses()
*/



    /*fun constructDDR(n: Int, size: Long): Memory? {
        if (size == 0L)
            return null

        val sa = startAddress(n)
        val mem = Memory(ports.outp, sa, sa + size, "DDRController$n", ACCESS.R_W)
        mem.endian = ByteOrder.BIG_ENDIAN
        return mem
    }

    val ddr0: Memory? = constructDDR(0, ddr0Size)
    val ddr1: Memory? = constructDDR(1, ddr1Size)
    val ddr2: Memory? = constructDDR(2, ddr2Size)
    val ddr3: Memory? = constructDDR(3, ddr3Size)*/

    val cs0_start: Long
        get() = DDR_CS0_BNDS.SA.toLong() shl 24
    val cs0_end: Long
        get() = DDR_CS0_BNDS.EA.toLong() shl 24
    val cs0_range: LongRange
        get() = cs0_start until cs0_end

    // TODO: other CS
    // MAX: 1 Gb
    fun startAddress(n: Int) = when (n) {
        0 -> 0x0000_0000L
        1 -> 0x4000_0000L
        2 -> 0x8000_0000L
        3 -> 0xC000_0000L
        else -> throw GeneralException("Wrong n: $n")
    }

    inner class Filter(parent: Module) : AddressTranslator(parent, "filter", BUS32) {
        override fun translate(ea: Long, ss: Int, size: Int, LorS: AccessAction): Long {
            if (ea in cs0_range) {
                return startAddress(0) + (ea - cs0_start)
            }
                //TODO: other CS

            return 0xFFFF_FFFF // Stub - to avoid wrong access
        }
    }

    val filter = Filter(this)

    /*init {
        ports.inp.connect(buses.intern)

        filter.ports.inp.connect(buses.intern)
        filter.ports.outp

        ports.outp.connect(buses.mem)
    }*/


    inner class DDR_CS_BNDS(val n: Int) : Register(ports.ctrl, 0x2000 + 8 * n.toLong(), Datatype.DWORD, "DDR_CS${n}_BNDS") {
        var SA by field(27..16)
        var EA by field(11..0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            log.severe { "$name: range is ${(SA shl 24).hex8}..${(EA shl 24).hex8}" }
        }

    }

    inner class DDR_CS_CONFIG(val n: Int) : Register(ports.ctrl, 0x2080 + 4 * n.toLong(), Datatype.DWORD, "DDR_CS${n}_CONFIG") {
        var CS_EN by bit(31)
        var AP_EN by bit(23)
        var ODT_RD_CFG by field(22..20)
        var ODT_WR_CFG by field(18..16)
        var BA_BITS_CS by field(15..14)
        var ROW_BITS_CS by field(10..8)
        var COL_BITS_CS by field(2..0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            log.severe { "$name: chip select is${ if (CS_EN.toBool()) "" else " not"} active" }
            log.severe { "$name: row bits: ${ROW_BITS_CS + 12}" }
            log.severe { "$name: column bits: ${COL_BITS_CS + 8}" }
        }
    }

    inner class DDR_CS_CONFIG_2(val n: Int) : Register(ports.ctrl, 0x20C0 + 4 * n.toLong(), Datatype.DWORD, "DDR_CS${n}_CONFIG_2") {
        var PASR_CFG by field(26..24)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            log.severe { "$name: Partial array self refresh is ${ if (PASR_CFG.toBool()) "enabled" else "disabled"}" }
        }
    }





    val DDR_CS0_BNDS = DDR_CS_BNDS(0)
    val DDR_CS1_BNDS = DDR_CS_BNDS(1)
    val DDR_CS2_BNDS = DDR_CS_BNDS(2)
    val DDR_CS3_BNDS = DDR_CS_BNDS(3)
    val DDR_CS0_CONFIG = DDR_CS_CONFIG(0)
    val DDR_CS1_CONFIG = DDR_CS_CONFIG(1)
    val DDR_CS2_CONFIG = DDR_CS_CONFIG(2)
    val DDR_CS3_CONFIG = DDR_CS_CONFIG(3)
    val DDR_CS0_CONFIG_2 = DDR_CS_CONFIG_2(0)
    val DDR_CS1_CONFIG_2 = DDR_CS_CONFIG_2(1)
    val DDR_CS2_CONFIG_2 = DDR_CS_CONFIG_2(2)
    val DDR_CS3_CONFIG_2 = DDR_CS_CONFIG_2(3)
    val DDR_TIMING_CFG_3 = object : Register(ports.ctrl, 0x2100, Datatype.DWORD, "DDR_TIMING_CFG_3") {
        var EXT_ACTTOPRE by bit(24)
        var EXT_REFREC by field(19..16)
        var EXT_CASLAT by bit(12)
        var CNTL_ADJ by field(2..0)
    }
    val DDR_TIMING_CFG_0 = object : Register(ports.ctrl, 0x2104, Datatype.DWORD, "DDR_TIMING_CFG_0", 0x0011_0105) {
        var RWT by field(31..30)
        var WRT by field(29..28)
        var RRT by field(27..26)
        var WWT by field(25..24)
        var ACT_PD_EXIT by field(22..20)
        var PRE_PD_EXIT by field(19..16)
        var ODT_PD_EXIT by field(11..8)
        var MRS_CYC by field(3..0)
    }
    val DDR_TIMING_CFG_1 = object : Register(ports.ctrl, 0x2108, Datatype.DWORD, "DDR_TIMING_CFG_1") {
       //TODO: Stub - not fully implemented
    }
    val DDR_TIMING_CFG_2 = object : Register(ports.ctrl, 0x210C, Datatype.DWORD, "DDR_TIMING_CFG_2") {
        //TODO: Stub - not fully implemented
    }
    val DDR_SDRAM_CFG = object : Register(ports.ctrl, 0x2110, Datatype.DWORD, "DDR_SDRAM_CFG", 0x0300_0000) {
        var MEM_EN by bit(31)
        var SREN by bit(30)
        var ECC_EN by bit(29)
        var RD_EN by bit(28)
        var SDRAM_TYPE by field(26..24)
        var DYN_PWR by bit(21)
        var DBW by field(20..19)
        var EIGHT_BE by bit(18)
        var THREET_EN by bit(16)
        var TWOT_EN by bit(15)
        var BA_INTLV_CTL by field(14..8)
        var x32_EN by bit(5)
        var PCHB8 by bit(4)
        var HSE by bit(3)
        var MEM_HALT by bit(1)
        var BI by bit(0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            log.severe { "$name: SDRAM interface logic is ${ if (MEM_EN.toBool()) "enabled" else "disabled"}" }
            log.severe { "$name: ECC is  ${ if (ECC_EN.toBool()) "enabled" else "ignored"}" }
            log.severe { "$name: Indicates ${if (RD_EN.toBool()) "registered" else "unbuffered"} DIMMs" }
            when (SDRAM_TYPE) {
                0b011 -> log.severe { "$name: DDR2 SDRAM" }
                0b111 -> log.severe { "$name: DDR3 SDRAM" }
            }
            log.severe { "$name: ${if (DBW.toBool()) "32" else "64"}-bit bus used" }
            log.severe { "$name: ${if (EIGHT_BE.toBool()) "8" else "4"}-beat bursts are used" }
            log.severe { "$name: THREET_EN = $THREET_EN" }
            log.severe { "$name: TWOT_EN = $TWOT_EN" }
            log.severe { "$name: BA_INTLV_CTL = $BA_INTLV_CTL" }
            log.severe { "$name: x32 = $x32_EN" }
            log.severe { "$name: PCHB8 = $PCHB8" }
            log.severe { "$name: HSE = $HSE" }
            log.severe { "$name: MEM_HALT = $MEM_HALT" }
            log.severe { "$name: BI = $BI" }
        }

    }
    val DDR_SDRAM_CFG_2 = object : Register(ports.ctrl, 0x2114, Datatype.DWORD, "DDR_SDRAM_CFG_2") {
        //TODO: Stub - not fully implemented
    }
    val DDR_SDRAM_MODE = object : Register(ports.ctrl, 0x2118, Datatype.DWORD, "DDR_SDRAM_MODE") {
        //TODO: Stub - not fully implemented
    }
    val DDR_SDRAM_MODE_2 = object : Register(ports.ctrl, 0x211C, Datatype.DWORD, "DDR_SDRAM_MODE_2") {
        //TODO: Stub - not fully implemented
    }
    val DDR_DDR_SDRAM_MD_CNTL = object : Register(ports.ctrl, 0x2120, Datatype.DWORD, "DDR_DDR_SDRAM_MD_CNTL") {
        //TODO: Stub - not fully implemented
    }
    val DDR_DDR_SDRAM_INTERVAL = object : Register(ports.ctrl, 0x2124, Datatype.DWORD, "DDR_DDR_SDRAM_INTERVAL") {
        //TODO: Stub - not fully implemented
    }
    val DDR_DDR_DATA_INIT = object : Register(ports.ctrl, 0x2128, Datatype.DWORD, "DDR_DDR_DATA_INIT") {
        //TODO: Stub - not fully implemented
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            log.severe { "$name: Init value is ${value.hex8}" }
        }
    }

    val DDR_DDR_SDRAM_CLK_CNTL = object : Register(ports.ctrl, 0x2130, Datatype.DWORD, "DDR_DDR_SDRAM_CLK_CNTL", 0x0200_0000) {
        //TODO: Stub - not fully implemented
    }
    val DDR_TIMING_CFG_4 = object : Register(ports.ctrl, 0x2160, Datatype.DWORD, "DDR_TIMING_CFG_4") {
        //TODO: Stub - not fully implemented
    }
    val DDR_TIMING_CFG_5 = object : Register(ports.ctrl, 0x2164, Datatype.DWORD, "DDR_TIMING_CFG_5") {
        //TODO: Stub - not fully implemented
    }
    val DDR_DDR_ZQ_CNTL = object : Register(ports.ctrl, 0x2170, Datatype.DWORD, "DDR_DDR_ZQ_CNTL") {
        //TODO: Stub - not fully implemented
    }
    val DDR_DDR_WRLVL_CNTL = object : Register(ports.ctrl, 0x2174, Datatype.DWORD, "DDR_DDR_WRLVL_CNTL") {
        //TODO: Stub - not fully implemented
    }
    val DDR_DDRCDR_1 = object : Register(ports.ctrl, 0x2B28, Datatype.DWORD, "DDR_DDRCDR_1") {
        //TODO: Stub - not fully implemented
    }
    val DDR_DDRCDR_2 = object : Register(ports.ctrl, 0x2B2C, Datatype.DWORD, "DDR_DDRCDR_2") {
        //TODO: Stub - not fully implemented
    }
    val DDR_ERR_INT_EN = object : Register(ports.ctrl, 0x2E48, Datatype.DWORD, "DDR_ERR_INT_EN") {
        //TODO: Stub - not fully implemented
    }
    val DDR_ERR_SBE = object : Register(ports.ctrl, 0x2E58, Datatype.DWORD, "DDR_ERR_SBE") {
        //TODO: Stub - not fully implemented
    }
}