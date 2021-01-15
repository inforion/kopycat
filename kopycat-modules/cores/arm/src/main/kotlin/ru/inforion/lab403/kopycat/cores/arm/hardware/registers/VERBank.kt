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
package ru.inforion.lab403.kopycat.cores.arm.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


class VERBank : ARegistersBankNG<AARMCore>("Virtualization Extension Registers", 3, 32) {
    inner class HCR : Register("hcr", 0) {
        var vm by bitOf(0)
        var swio by bitOf(1)
        var ptw by bitOf(2)
        var fmo by bitOf(3)
        var imo by bitOf(4)
        var amo by bitOf(5)
        var vf by bitOf(6)
        var vi by bitOf(7)
        var va by bitOf(8)
        var fb by bitOf(9)
        var bsu by fieldOf(11..10)
        var dc by bitOf(12)
        var twi by bitOf(13)
        var twe by bitOf(14)
        var tid0 by bitOf(15)
        var tid1 by bitOf(16)
        var tid2 by bitOf(17)
        var tid3 by bitOf(18)
        var tsc by bitOf(19)
        var tidcp by bitOf(20)
        var tac by bitOf(21)
        var tsw by bitOf(22)
        var tpc by bitOf(23)
        var tpu by bitOf(24)
        var ttlb by bitOf(25)
        var tvm by bitOf(26)
        var tge by bitOf(27)
    }

    val hcr = HCR()

    val hsr = Register("hsr", 1)
    val hstr = Register("hstr", 2)
}