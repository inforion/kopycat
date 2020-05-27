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
package ru.inforion.lab403.kopycat.cores.mips.enums

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get


enum class Cause(val pos: Int) {

    EXC_L(2),
    EXC_H(6), // Exception code
    IP0(8),   // Request software interrupt 0
    IP1(9),   // Request software interrupt 1
    IP2(10),  // Hardware interrupt 0
    IP3(11),  // Hardware interrupt 1
    IP4(12),  // Hardware interrupt 2
    IP5(13),  // Hardware interrupt 3
    IP6(14),  // Hardware interrupt 4
    IP7(15),  // Hardware interrupt 5, timer or performance counter interrupt
    WP(22),
    IV(23),   // Use the general exception vector or a special interrupt vector
    CE_L(28),
    CE_H(29),
    TI(30),
    BD(31);

    infix fun from(value: Long): Long = value[this.pos]

    companion object {
        fun extract(value: Long): Map<Cause, Int> = Cause.values().associate { Pair(it, (it from value).asInt) }
    }
}