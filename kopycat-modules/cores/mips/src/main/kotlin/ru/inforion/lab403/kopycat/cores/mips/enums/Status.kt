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


enum class Status(val pos: Int) {
    IE(0),
    EXL(1),
    ERL(2),
    R0(3),
    UM(4),
    UX(5),
    SX(6),
    KX(7),
//    IPL_L(10),
//    IPL_H(15),
    IM0(8),
    IM1(9),
    IM2(10),
    IM3(11),
    IM4(12),
    IM5(13),
    IM6(14),
    IM7(15),
    NMI(19),
    SR(20),
    TS(21),
    BEV(22),
    PX(23),
    MX(24),
    RE(25),
    FR(26),
    RP(27),
    CU0(28),
    CU1(29),
    CU2(30),
    CU3(31);

    infix fun from(value: Long): Long = value[this.pos]

    companion object {
        fun extract(value: Long): Map<Status, Int> = values().associate { Pair(it, (it from value).asInt) }
    }
}