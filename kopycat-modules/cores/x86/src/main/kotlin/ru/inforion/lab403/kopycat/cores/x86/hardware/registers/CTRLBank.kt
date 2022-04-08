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

import ru.inforion.lab403.common.extensions.clr
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.CTRLR
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class CTRLBank(val core: x86Core) : ARegistersBankNG<x86Core>("Control Registers", CTRLR.values().size, 64) {

    inner class CR0 : Register("cr0", 0) {
        var pe by bitOf(0)
        var mp by bitOf(1)
        var em by bitOf(2)
        var ts by bitOf(3)
        var et by bitOf(4)
        var ne by bitOf(5)
        var wp by bitOf(16)
        var am by bitOf(18)
        var nw by bitOf(29)
        var cd by bitOf(30)
        var pg by bitOf(31)
    }

    inner class CR3 : Register("cr3", 3) {

        val PML4Address: ULong get() = value clr 11..0

        override var value: ULong
            get() = super.value
            set(value) {
                AOperand.log.fine { "[${core.pc.hex}] CR3 register changed to ${value.hex} -> paging cache invalidated!" }
                super.value = value
                core.mmu.invalidatePagingCache()
            }
    }

    inner class CR4 : Register("cr4", 4) {
        var vme by bitOf(0)
        var pvi by bitOf(1)
        var tsd by bitOf(2)
        var de by bitOf(3)
        var pse by bitOf(4)
        var pae by bitOf(5)
        var mce by bitOf(6)
        var pge by bitOf(7)
        var pce by bitOf(8)
        var osfxsr by bitOf(9)
        var osxmmexcpt by bitOf(10)
        var vmxe by bitOf(13)
        var smxe by bitOf(14)
        var fsgsbase by bitOf(16)
        var pcide by bitOf(17)
        var osxsave by bitOf(18)
        var smep by bitOf(20)
        var smap by bitOf(21)
        var pke by bitOf(22)
    }

    val cr0 = CR0()
    val cr1 = Register("cr1", 1)
    val cr2 = Register("cr2", 2)
    val cr3 = CR3()
    val cr4 = CR4()
}