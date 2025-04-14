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
@file:Suppress("MemberVisibilityCanBePrivate", "PropertyName", "unused")

package ru.inforion.lab403.kopycat.modules.p2020

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.field



class PIC(parent: Module, name: String) : Module(parent, name) {

    inner class Ports : ModulePorts(this) {
//        val inp = Slave("in", BUS32)
        val ctrl = Port("ctrl")
    }

    override val ports = Ports()

    inner class PIC_IPIVPRn(val n: Int) : Register(ports.ctrl, 0x4_10A0uL + 16 * n, DWORD, "PIC_IPIVPR$n", default = 0x80000000u) {

        var MSK by bit(31)
        var A by bit(30)
        var PRIORITY by field(19..16)
        var VECTOR by field(15..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "$name: MSK=$MSK, A=$A, PRIORITY=${PRIORITY.hex8}, VECTOR=${VECTOR.hex8}" }
        }

    }

    inner class PIC_TFRRn(val n: Int) : Register(ports.ctrl, 0x4_10F0uL + 4096 * n, DWORD, "PIC_TFRR$n")

    inner class PIC_GTBCRAn(val n: Int) : Register(ports.ctrl, 0x4_1110uL + 64 * n, DWORD, "PIC_GTBCRA$n", default = 0x80000000u) {
        var CI by bit(31)
        var BASE_CNT by field(30..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "$name: CI=$CI, BASE_CNT=$BASE_CNT" }
        }
    }

    inner class PIC_GTBCRBn(val n: Int) : Register(ports.ctrl, 0x4_2110uL + 64 * n, DWORD, "PIC_GTBCRB$n", default = 0x80000000u) {
        var CI by bit(31)
        var BASE_CNT by field(30..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "$name: CI=$CI, BASE_CNT=$BASE_CNT" }
        }
    }


    inner class PIC_GTVPRAn(val n: Int) : Register(ports.ctrl, 0x4_1120uL + 64 * n, DWORD, "PIC_GTVPRA$n", default = 0x80000000u) {

        var MSK by bit(31)
        var A by bit(30)
        var PRIORITY by field(19..16)
        var VECTOR by field(15..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "$name: MSK=$MSK, A=$A, PRIORITY=${PRIORITY.hex8}, VECTOR=${VECTOR.hex8}" }
        }

    }

    inner class PIC_GTDRAn(val n: Int) : Register(ports.ctrl, 0x4_1130uL + 64 * n, DWORD, "PIC_GTDRA$n", default = 0x00000001u) {

        var P1 by bit(1)
        var P0 by bit(0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "$name: P0=$P0, P1=$P1" }
        }

    }

    inner class PIC_TCRn(val n: Int) : Register(ports.ctrl, 0x4_1300uL + 4096 * n, DWORD, "PIC_TCRn$n") {
        var ROVR by field(26..24)
        var RTM by bit(16)
        var CLKR by field(9..8)
        var CASC by field(2..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)

            log.severe { "$name: ROVR=$ROVR, RTM=$RTM, CLKR=$CLKR, CASC=$CASC" }
        }

    }

    inner class PIC_GTVPRBn(val n: Int) : Register(ports.ctrl, 0x4_2120uL + 64 * n, DWORD, "PIC_GTVPRB$n", default = 0x80000000u) {

        var MSK by bit(31)
        var A by bit(30)
        var PRIORITY by field(19..16)
        var VECTOR by field(15..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "$name: MSK=$MSK, A=$A, PRIORITY=${PRIORITY.hex8}, VECTOR=${VECTOR.hex8}" }
        }

    }



    inner class PIC_EIVPRn(val n: Int) : Register(ports.ctrl, 0x5_0000uL + 32 * n, DWORD, "PIC_EIVPR$n", default = 0x80000000u) {

        var MSK by bit(31)
        var A by bit(30)
        var P by bit(23)
        var S by bit(22)
        var PRIORITY by field(19..16)
        var VECTOR by field(15..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "$name: MSK=$MSK, A=$A, P=$P, S=$S, PRIORITY=${PRIORITY.hex8}, VECTOR=${VECTOR.hex8}" }
        }

    }

    inner class PIC_IIVPRn(val n: Int) : Register(ports.ctrl, 0x5_0200uL + 32 * n, DWORD, "PIC_IIVPR$n", default = 0x80800000u) {

        var MSK by bit(31)
        var A by bit(30)
        var P by bit(23)
        var PRIORITY by field(19..16)
        var VECTOR by field(15..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "$name: MSK=$MSK, A=$A, P=$P, PRIORITY=${PRIORITY.hex8}, VECTOR=${VECTOR.hex8}" }
        }

    }

    inner class PIC_MIVPRn(val n: Int) : Register(ports.ctrl, 0x5_1600uL + 32 * n, DWORD, "PIC_MIVPR$n", default = 0x80000000u) {

        var MSK by bit(31)
        var A by bit(30)
        var PRIORITY by field(19..16)
        var VECTOR by field(15..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "$name: MSK=$MSK, A=$A, PRIORITY=${PRIORITY.hex8}, VECTOR=${VECTOR.hex8}" }
        }

    }


    inner class PIC_MSIVPRn(val n: Int) : Register(ports.ctrl, 0x5_1C00uL + 32 * n, DWORD, "PIC_MSIVPR$n", default = 0x80000000u) {

        var MSK by bit(31)
        var A by bit(30)
        var PRIORITY by field(19..16)
        var VECTOR by field(15..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "$name: MSK=$MSK, A=$A, PRIORITY=${PRIORITY.hex8}, VECTOR=${VECTOR.hex8}" }
        }

    }

    //TODO: NOT FULLY IMPLEMENTED
    val PIC_CTPR = object : Register(ports.ctrl, 0x4_0080u, DWORD, "PIC_CTPR", default = 0xFu) {
        var TASKP by field(3..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value and 0xFu)
            log.severe { "$name: TASKP = ${TASKP.hex2}"}
        }
    }
    val PIC_IACK = object : Register(ports.ctrl, 0x4_00A0u, DWORD, "PIC_IACK", writable = false) {
        var VECTOR by field(15..0)
    }
    val PIC_EOI = object : Register(ports.ctrl, 0x4_00B0u, DWORD, "PIC_EOI", readable = false) {
        var EOI_CODE by field(3..0)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value and 0xFu)
            log.severe { "$name: interrupt end, EOI_CODE = ${EOI_CODE.hex2}"}
        }
    }

    val PIC_FRR = Register(ports.ctrl, 0x4_1000u, DWORD, "PIC_FRR", writable = false, default = 0x6B0002u)
    val PIC_GCR = object : Register(ports.ctrl, 0x4_1020u, DWORD, "PIC_GCR") {
        var RST by bit(31)
        var M by bit(29)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)
            log.severe { "$name: RST=$RST, M=$M"}
            if (RST.truth)
                RST = 0
        }
    }

    val PIC_PIR = object : Register(ports.ctrl, 0x4_1090u, DWORD, "PIC_PIR") {
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            if (value[0].truth)
                log.warning { "$name: core0_hreset" }
            if (value[1].truth)
                log.warning { "$name: core1_hreset" }
        }
    }
    val PIC_IPIVPR0 = PIC_IPIVPRn(0) // 0x4_10A0
    val PIC_IPIVPR1 = PIC_IPIVPRn(1) // 0x4_10B0
    val PIC_IPIVPR2 = PIC_IPIVPRn(2) // 0x4_10C0
    val PIC_IPIVPR3 = PIC_IPIVPRn(3) // 0x4_10D0
    val PIC_SVR = Register(ports.ctrl, 0x4_10E0u, DWORD, "PIC_SVR", default = 0x0000FFFFu)
    val PIC_TFRR0 = PIC_TFRRn(0)     // 0x4_10F0
    val PIC_GTBCRA0 = PIC_GTBCRAn(0) // 0x4_1110
    val PIC_GTVPRA0 = PIC_GTVPRAn(0) // 0x4_1120
    val PIC_GTDRA0 = PIC_GTDRAn(0)   // 0x4_1130
    val PIC_GTBCRA1 = PIC_GTBCRAn(1) // 0x4_1150
    val PIC_GTVPRA1 = PIC_GTVPRAn(1) // 0x4_1160
    val PIC_GTDRA1 = PIC_GTDRAn(1)   // 0x4_1170
    val PIC_GTBCRA2 = PIC_GTBCRAn(2) // 0x4_1190
    val PIC_GTVPRA2 = PIC_GTVPRAn(2) // 0x4_11A0
    val PIC_GTDRA2 = PIC_GTDRAn(2)   // 0x4_11B0
    val PIC_GTBCRA3 = PIC_GTBCRAn(3) // 0x4_11D0
    val PIC_GTVPRA3 = PIC_GTVPRAn(3) // 0x4_11E0
    val PIC_GTDRA3 = PIC_GTDRAn(3)   // 0x4_11F0
    val PIC_TCRA = PIC_TCRn(0)       // 0x4_1300
    val PIC_MER = Register(ports.ctrl, 0x4_1500u, DWORD, "PIC_MER")
    val PIC_TFRR1 = PIC_TFRRn(1)     // 0x4_20F0
    val PIC_GTBCRB0 = PIC_GTBCRBn(0) // 0x4_2110
    val PIC_GTVPRB0 = PIC_GTVPRBn(0) // 0x4_2120
    val PIC_GTBCRB1 = PIC_GTBCRBn(1) // 0x4_2150
    val PIC_GTVPRB1 = PIC_GTVPRBn(1) // 0x4_2160
    val PIC_GTVPRB2 = PIC_GTVPRBn(2) // 0x4_21A0
    val PIC_GTVPRB3 = PIC_GTVPRBn(3) // 0x4_21E0
    val PIC_GTBCRB2 = PIC_GTBCRBn(2) // 0x4_2190
    val PIC_GTBCRB3 = PIC_GTBCRBn(3) // 0x4_21D0
    val PIC_TCRB = PIC_TCRn(1)       // 0x4_2300


    val PIC_EIVPRArray = Array(12) { i -> PIC_EIVPRn(i) } // 0x5_0000 + 0x20*i -> 0x5_0160
    val PIC_IIVPRArray = Array(64) { i -> PIC_IIVPRn(i) } // 0x5_0200 + 0x20*i -> 0x5_09E0
    val PIC_MIVPRArray = Array(8) { i -> PIC_MIVPRn(i) } // 0x5_1600 + 0x20*i -> 0x5_16E0
    val PIC_MSIVPRArray = Array(8) { i -> PIC_MSIVPRn(i) } // 0x5_1C00 + 0x20*i -> 0x5_1CE0


    val PIC_CTPR_CPU0 = Register(ports.ctrl, 0x6_0080u, DWORD, "PIC_CTPR_CPU0", default = 0x0000000Fu)
    val PIC_IACK_CPU0 = object : Register(ports.ctrl, 0x6_00A0u, DWORD, "PIC_IACK_CPU0", writable = false) {
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            return PIC_SVR.data
        }
    }

}