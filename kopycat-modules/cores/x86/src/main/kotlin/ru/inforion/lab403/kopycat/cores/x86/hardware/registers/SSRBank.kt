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
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class SSRBank(val core: x86Core) : ARegistersBankNG<x86Core>("Segment Registers", 6, 64) {

    open inner class SSR(name: String, id: Int, default: ULong = 0u) : Register(name, id, default, Datatype.WORD) {
        override var value: ULong
            get() = super.value
            set(value) {
                super.value = value
                core.mmu.updateCache(this)
            }
    }

    inner class CS : SSR("cs", 1, 0xFFFF000u) {
        var ti by bitOf(2)
        var cpl by fieldOf(1..0)
    }

    val es = SSR("es", 0)
    val cs = CS()
    val ss = SSR("ss", 2)
    val ds = SSR("ds", 3)
    val fs = SSR("fs", 4)
    val gs = SSR("gs", 5)
}