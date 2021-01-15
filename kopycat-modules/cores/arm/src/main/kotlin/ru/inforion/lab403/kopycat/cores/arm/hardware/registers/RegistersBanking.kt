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

import ru.inforion.lab403.kopycat.cores.arm.enums.ProcessorMode
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


class RegistersBanking(val mode: ProcessorMode) : ARegistersBankNG<AARMCore>(
        "ARM Register bank for mode ${mode.name}", 20,  32) {

    val r8 = Register("r8", 8)
    val r9 = Register("r9", 9)
    val r10 = Register("r10", 10)
    val r11 = Register("r11", 11)
    val r12 = Register("r12", 12)
    val sp = Register("sp", 13)
    val lr = Register("lr", 14)

    val spsr = Register("spsr", 19)
}