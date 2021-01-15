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
@file:Suppress("unused")

package ru.inforion.lab403.kopycat.cores.arm.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


class VMSABank : ARegistersBankNG<AARMCore>(
        "Virtual Memory System Architecture System control registers", 11, 32) {

    enum class Implementer(val data: Long) {
        ArmLimited('A'.toLong()),
        DigitalEquipmentCorporation('D'.toLong()),
        Motorola('M'.toLong()),
        FreescaleSemiconfuctorInc('M'.toLong()),
        QualcommInc('Q'.toLong()),
        MarvellSemiconductorInc('V'.toLong()),
        IntelCorporation('i'.toLong())
    }

    enum class Architecture(val data: Long) {
        ARMv4(0x1),
        ARMv4T(0x2),
        ARMv5(0x3), // Obsolete
        ARMv5T(0x4),
        ARMv5TE(0x5),
        ARMv5TEJ(0x6),
        ARMv6(0x7),
        DefinedByCPUIDScheme(0xF)
    }

    enum class CacheTypeFormat(val data: Long) {
        ARMv6(0b000),
        ARMv7(0b100)
    }

    enum class L1IP(val data: Long) {
        Reserved(0b00),
        AIVIVT(0b01),
        VIPT(0b10),
        PIPT(0b11)
    }

    // ARMv6 implementation
    inner class SCTLR : Register("sctlr", 0, default=0xC50074) {
        val mask = 0x4FE07807L

        var te by bitOf(30)
        var afe by bitOf(29)
        //        var tre by bitOf(28) // yet not introduced
        var nmfi by bitOf(27)
        var l2 by bitOf(26)
        var ee by bitOf(25)
        var ve by bitOf(24)
        var xp by bitOf(23)
        var u by bitOf(22)
        var fi by bitOf(21)
        var ha by bitOf(17)
        var l4 by bitOf(15)
        var rr by bitOf(14)
        var v by bitOf(13)
        var i by bitOf(12)
        var z by bitOf(11)
        var w by bitOf(3)
        var c by bitOf(2)
        var m by bitOf(0)
    }

    val sctlr = SCTLR()

    val ttbr0 = object : Register("ttbr0", 1) {
        val address: Long get() = value ushr (14 - ttbcr.n.toInt())
    }

    val ttbr1 = Register("ttbr1", 2)

    inner class TTBCR : Register("ttbcr", 3) {
        var eae by bitOf(31)
        var n by fieldOf(2..0)
    }

    val ttbcr = TTBCR()

    val dacr = Register("dacr", 4)

    inner class MIDR : Register("midr", 5) {
        var implementer by fieldOf(31..24)
        var variant by fieldOf(23..20)
        var architecture by fieldOf(19..16)
        var primaryPartNumber by fieldOf(15..4)
        var revision by fieldOf(3..0)
    }

    val midr = MIDR()

    inner class CTR : Register("ctr", 6) {
        var format by fieldOf(31..29)
        var cwg by fieldOf(27..24)
        var erg by fieldOf(23..20)
        var dminLine by fieldOf(19..16)
        var l1lp by fieldOf(15..14)
        var iminLine by fieldOf(3..0)

    }

    val ctr = CTR()

    val dfar = Register("dfar", 7)

    inner class DFSR : Register("dfsr", 8) {
        var bits13_0 by fieldOf(13..0)
    }

    val dfsr = DFSR()

    val contextidr = Register("contextidr", 9)
    val tpidruro = Register("tpidruro", 10)
}