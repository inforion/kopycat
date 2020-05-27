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
package ru.inforion.lab403.kopycat.cores.base.enums

enum class Datatype(val id: Int, val bits: Int, val bytes: Int, val msb: Int, val lsb: Int) {
    UNKNOWN(0, 0, 0, 0, 0),

    BYTE(0, 8, 1, 7, 0),
    WORD(1, 16, 2, 15, 0),
    DWORD(2, 32, 4, 31, 0),
    QWORD(3, 64, 8, 63, 0),

    TRIBYTE(20, 24, 3, 23, 0),
    FWORD(21, 48, 6, 47, 0),   // LGDT, LIDT of x86 processor
    FPU80(22, 80, 10, 79, 0),  // FRU of x86 processor

    BYTES5(23, 40, 5, 39, 0),
    BYTES7(25, 56, 7, 55, 0);
}