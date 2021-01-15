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


enum class GPR(val id: Int) {
    ZERO(0),
    AT(1),

    V0(2),
    V1(3),

    A0(4),
    A1(5),
    A2(6),
    A3(7),

    T0(8),
    T1(9),
    T2(10),
    T3(11),
    T4(12),
    T5(13),
    T6(14),
    T7(15),

    S0(16),
    S1(17),
    S2(18),
    S3(19),
    S4(20),
    S5(21),
    S6(22),
    S7(23),

    T8(24),
    T9(25),

    K0(26),
    K1(27),

    GP(28),
    SP(29),
    FP(30),
    RA(31);
}
