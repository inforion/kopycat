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
package ru.inforion.lab403.kopycat.modules.syscores.sha1.enums

object GPR {
    const val X0 = 0  // extension register - general purpose
    const val X1 = 1
    const val X2 = 2
    const val X3 = 3

    const val A = 4  // SHA1 register A
    const val B = 5
    const val C = 6
    const val D = 7
    const val E = 8

    const val MD = 9  // processor mode register (0 - normal mode, 1 - just counter PC/CT)

    const val RD = 10  // round register - round of SHA1

    const val FK = 27  // f*ck up register - must be zero

    const val CT = 28  // counter register - increment on each instruction
    const val MF = 29  // memory start register - where operating memory starts (load/store operations)
    const val ID = 30  // identifier register - do nothing
    const val PC = 31  // program counter register - point to current memory address
}