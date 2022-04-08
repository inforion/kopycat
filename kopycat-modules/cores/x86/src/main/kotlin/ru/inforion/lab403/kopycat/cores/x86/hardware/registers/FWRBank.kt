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
package ru.inforion.lab403.kopycat.cores.x86.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.x86.enums.FWR
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class FWRBank(val core: x86Core) : ARegistersBankNG<x86Core>("FWR Control Registers", FWR.values().size, bits = 64) {

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
        var top by bitOf(11)
        var c3  by bitOf(14)
        var b   by bitOf(15)
    }

    inner class CWR : Register("CWR", 1, dtype = Datatype.WORD) {
        var i  by bitOf(0)
        var d  by bitOf(1)
        var z  by bitOf(2)
        var o  by bitOf(3)
        var u  by bitOf(4)
        var p  by bitOf(5)
        var pc by bitOf(8)
        var rc by bitOf(10)
    }

    val FPUStatusWord = SWR()
    val FPUControlWord = CWR()
    val FPUTagWord = Register("TWR", 2, dtype = Datatype.WORD)
    val FPUDataPointer = Register("FDP", 3, dtype = Datatype.WORD)
    val FPULastInstructionOpcode = Register("LIO", 4, dtype = Datatype.WORD)
    val FPUInstructionPointer = Register("FIP", 5, dtype = Datatype.QWORD)
}