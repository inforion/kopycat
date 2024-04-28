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
package ru.inforion.lab403.kopycat.cores.x86.hardware.registers

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.x86.enums.FWR
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class FWRBank(val core: x86Core) : ARegistersBankNG<x86Core>("FWR Control Registers", FWR.values().size, bits = 64) {
    // 00 01 10 11
    // SP -- DP DEP
    enum class PrecisionControl(val cw: Int) {
        Single(0b00),
        Invalid(0b01),
        Double(0b10),
        ExtendedDouble(0b11),
    }

    // 00   01   10  11
    // RNE -INF +INF RTZ
    enum class RoundControl(val cw: Int) {
        RoundToNearestEven(0b00),
        RoundTowardsNegative(0b01),
        RoundTowardsPositive(0b10),
        RoundTowardZero(0b11),
    }

    enum class TagValue(val tw: Int) {
        Valid(0b00),
        Zero(0b01),
        Special(0b10), //10 — Special: invalid (NaN, unsupported), infinity, or denormal
        Empty(0b11),
    }

    inner class SWR : Register("SWR", 0, dtype = Datatype.WORD) {
        var ie  by bitOf(0)
        var de  by bitOf(1)
        var xe  by bitOf(2)
        var oe  by bitOf(3)
        var ue  by bitOf(4)
        var pe  by bitOf(5)
        var sf  by bitOf(6)
        var es  by bitOf(7)
        var c0  by bitOf(8)
        var c1  by bitOf(9)
        var c2  by bitOf(10)
        var top by fieldOf(13..11)
        var c3  by bitOf(14)
        var b   by bitOf(15)
    }

    inner class CWR : Register("CWR", 1, dtype = Datatype.WORD, default = 0x37fuL) {
        /** Exception masks - Invalid Inputs */
        var i by bitOf(0)
        /** Exception masks - Denormal Inputs */
        var d by bitOf(1)
        /** Exception masks - Divide-by-Zero */
        var z by bitOf(2)
        /** Exception masks - Overflow */
        var o by bitOf(3)
        /** Exception masks - Underflow */
        var u by bitOf(4)
        /** Exception masks - Precision */
        var p by bitOf(5)

        /**
         * Precision Control - Single Precision (SP), Double Precision (DP), Double Extended Precision (DEP).
         *
         * @see PrecisionControl
         */
        var pc // by fieldOf(9..8)
            get() = PrecisionControl.values().find { it.cw == value[9..8].int }!!
            set(v) { value = value.insert(v.cw.ulong_z, 9..8) }

        /**
         * Round Control - RoundTiesToEven / RoundToNearestEven (RNE),
         * RoundTowardsNegative (-INF), RoundTowardsPositive (+INF), RoundTowardZero (RTZ).
         *
         * @see RoundControl
         */
        var rc // by fieldOf(11..10)
            get() = RoundControl.values().find { it.cw == value[11..10].int }!!
            set(v) { value = value.insert(v.cw.ulong_z, 11..10) }
    }

    inner class TWR : Register("TWR", 2, dtype = Datatype.WORD, default = 0xffffuL) {
        operator fun get(i: Int) = TagValue.values().find {
            it.tw == value[i * 2 + 1..i * 2].int
        }!!

        operator fun set(i: Int, x: TagValue) {
            value = value.insert(x.tw.ulong_z, i * 2 + 1..i * 2)
        }
    }

    val FPUStatusWord = SWR()
    val FPUControlWord = CWR()
    val FPUTagWord = TWR()
    val FPUDataPointer = Register("FDP", 3, dtype = Datatype.WORD)
    val FPULastInstructionOpcode = Register("LIO", 4, dtype = Datatype.WORD)
    val FPUInstructionPointer = Register("FIP", 5, dtype = Datatype.QWORD)
}
