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
package ru.inforion.lab403.kopycat.cores.mips.hardware.registers

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


class FCRBank : ARegistersBankNG<MipsCore>("FPU Control Registers", 32, 32) {
    val fir = Register("fir", 0)

    inner class FCCR : Register("fccr", 25) {
        var fcc0 by bitOf(23)
        var fcc1 by bitOf(25)
        var fcc2 by bitOf(26)
        var fcc3 by bitOf(27)
        var fcc4 by bitOf(28)
        var fcc5 by bitOf(29)
        var fcc6 by bitOf(30)
        var fcc7 by bitOf(31)

        fun fccn(n: Int) = when (n) {
            0 -> fcc0
            1 -> fcc1
            2 -> fcc2
            3 -> fcc3
            4 -> fcc4
            5 -> fcc5
            6 -> fcc6
            7 -> fcc7
            else -> throw RuntimeException("Invalid fcc bit")
        }

        fun fccn(n: Int, value: Boolean) = when (n) {
            0 -> fcc0 = value
            1 -> fcc1 = value
            2 -> fcc2 = value
            3 -> fcc3 = value
            4 -> fcc4 = value
            5 -> fcc5 = value
            6 -> fcc6 = value
            7 -> fcc7 = value
            else -> throw RuntimeException("Invalid fcc bit")
        }
    }

    val fccr = FCCR()

    val fexr = Register("fexr", 26)
    val fenr = Register("fenr", 28)

    inner class FCSR : Register("fcsr", 31) {
        override var value: ULong
            get() = super.value

            set(value) {
                super.value = super.value
                    // 22-21. If these bits are not implemented, they must be ignored on write and read as zero.
                    .insert(value[31..23], 31..23)
                    //  20-18 reserved
                    .insert(value[17..0], 17..0)
            }
    }

    val fcsr = FCSR()
}
