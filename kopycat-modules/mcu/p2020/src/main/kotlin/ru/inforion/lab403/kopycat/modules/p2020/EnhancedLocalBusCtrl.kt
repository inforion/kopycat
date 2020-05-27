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


 
class EnhancedLocalBusCtrl(parent: Module, name: String, val romResetAddress: Long = 0xFE00_0000) : Module(parent, name) {

    inner class Ports : ModulePorts(this) {
        val inp: Translator
            get() = filter.ports.inp
        val outp: Master
            get() = filter.ports.outp
        val ctrl = Slave("ctrl", BUS32)
    }

    override val ports = Ports()


    // MAX: 512 Mb
    fun startAddress(n: Int) = when (n) {
        0 -> 0x0000_0000L
        1 -> 0x2000_0000L
        2 -> 0x4000_0000L
        3 -> 0x6000_0000L
        4 -> 0x8000_0000L
        5 -> 0xA000_0000L
        6 -> 0xC000_0000L
        7 -> 0xE000_0000L
        else -> throw GeneralException("Wrong n: $n")
    }

    fun segRange(n: Int): LongRange {
        val ba = getBR(n).BA.toLong() shl 15
        val am = getOR(n).AM.toLong() shl 15
        return ba..(ba + (am.inv() and 0xFFFF_FFFF))
    }


    inner class Filter(parent: Module) : AddressTranslator(parent, "filter", BUS32) {
        override fun translate(ea: Long, ss: Int, size: Int, LorS: AccessAction): Long {
            // Bypass ignored
            if (eLBC_LBCR.LDIS == 0) {
                for (i in 0..7) {
                    val br = getBR(i)
                    val range = segRange(i)
                    if (br.V.toBool() && (ea in range))
                        return startAddress(i) + (ea - range.first)
                }
            }
            return 0xFFFF_FFFF // Stub - to avoid wrong access
        }
    }

    val filter = Filter(this)

    enum class Mode(val msel: Int) {
        GPCM(0),
        FCM(1),
        UPMA(4),
        UPMB(5),
        UPMC(6);
    }


    fun getBR(n: Int): ELBC_BR = when (n) {
        0 -> eLBC_BR0
        1 -> eLBC_BR1
        2 -> eLBC_BR2
        3 -> eLBC_BR3
        4 -> eLBC_BR4
        5 -> eLBC_BR5
        6 -> eLBC_BR6
        7 -> eLBC_BR7
        else -> throw GeneralException("Wrong n: $n")
    }

    fun getOR(n: Int): ELBC_OR = when (n) {
        0 -> eLBC_OR0
        1 -> eLBC_OR1
        2 -> eLBC_OR2
        3 -> eLBC_OR3
        4 -> eLBC_OR4
        5 -> eLBC_OR5
        6 -> eLBC_OR6
        7 -> eLBC_OR7
        else -> throw GeneralException("Wrong n: $n")
    }


    //TODO: not fully implemented
    inner class ELBC_BR(val n: Int) : Register(ports.ctrl, 0x5000 + 8 * n.toLong(), Datatype.DWORD, "eLBC_BR$n") {
        var BA by field(31..15)
        var PS by field(12..11)
        var DECC by field(10..9)
        var WP by bit(8)
        var MSEL by field(7..5)
        var V by bit(0)


        override fun reset() {
            super.reset()
            if (n == 0) {
                V = 1
                BA = romResetAddress.toInt() shr 15
            }
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            log.severe {"eLBC_BR$n: V = $V"}
            when (MSEL) {
                Mode.GPCM.msel -> log.severe {"eLBC_BR$n: GPCM mode"}
                Mode.FCM.msel -> log.severe {"eLBC_BR$n: FCM mode"}
                else -> throw GeneralException("eLBC_BR$n: unimplemented MSEL mode: $MSEL")
            }
            log.severe { "$name: Base address = ${(BA shl 15).hex8}" }
        }

    }


    //TODO: not fully implemented
    inner class ELBC_OR(val n: Int) : Register(ports.ctrl, 0x5004 + 8 * n.toLong(), Datatype.DWORD, "eLBC_OR$n") {
        // GPCM mode (eLBC_ORg)
        var AM by field(31..15)
        var BCTLD by bit(12)
        var CSNT by bit(11)
        var ACS by field(10..9)
        var XACS by bit(8)
        var SCYg by field(7..4)
        var SETA by bit(3)
        var TRLX by bit(2)
        var EHTR by bit(1)
        var EAD by bit(0)

        // FCM mode (eLBC_ORf)
        var BCRLD by bit(12)
        var PGS by bit(10)
        var CSCT by bit(9)
        var CHT by field(8..7)
        var SCYf by field(6..4)
        var RST by bit(3)
        //var TRLX by bit(2)
        //var EHTR by bit(1)


        override fun reset() {
            super.reset()
            if (n == 0)
                data = 0x000_0FF7
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            val br = getBR(n)
            when (br.MSEL) {
                Mode.GPCM.msel -> {
                    log.severe { "$name: Mask = ${(AM shl 15).hex8}" }
                }
                Mode.FCM.msel -> {
                    log.severe { "$name: Mask = ${(AM shl 15).hex8}" }
                }
                else -> throw GeneralException("Unimplemented eLBC mode")
            }
            super.write(ea, ss, size, value)
        }
    }

    inner class ELBC_LBCR : Register(ports.ctrl, 0x50D0, Datatype.DWORD, "eLBC_LBCR") {
        var LDIS by bit(31)
        var BCTLC by field(23..22)
        var AHD by bit(21)
        var LPBSE by bit(20)
        var EPAR by bit(19)
        var BMT by field(15..8)
        var BMTPS by field(3..0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            log.severe { "$name: Local bus ${if (LDIS.toBool()) "disabled" else "enabled"}" }
        }
    }

    inner class ELBC_LCCR : Register(ports.ctrl, 0x50D4, Datatype.DWORD, "eLBC_LCCR") {
        var PBYP by bit(31)
        var EADC by field(17..16)
        var CLKDIV by field(4..0)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            log.severe { "$name: Local bus ${if (PBYP.toBool()) "bypassed" else "enabled"}" }
        }
    }


    //TODO: Not all registers are implemented

    val eLBC_BR0 = ELBC_BR(0)
    val eLBC_OR0 = ELBC_OR(0)

    val eLBC_BR1 = ELBC_BR(1)
    val eLBC_OR1 = ELBC_OR(1)

    val eLBC_BR2 = ELBC_BR(2)
    val eLBC_OR2 = ELBC_OR(2)

    val eLBC_BR3 = ELBC_BR(3)
    val eLBC_OR3 = ELBC_OR(3)

    val eLBC_BR4 = ELBC_BR(4)
    val eLBC_OR4 = ELBC_OR(4)

    val eLBC_BR5 = ELBC_BR(5)
    val eLBC_OR5 = ELBC_OR(5)

    val eLBC_BR6 = ELBC_BR(6)
    val eLBC_OR6 = ELBC_OR(6)

    val eLBC_BR7 = ELBC_BR(7)
    val eLBC_OR7 = ELBC_OR(7)



    val eLBC_LBCR = ELBC_LBCR()
    val eLBC_LCCR = ELBC_LCCR()

}