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
package ru.inforion.lab403.kopycat.veos.loader.peloader.enums

enum class ImageFileMachine(val value: Int) {

    UNKNOWN(0),
    I860(0x014d),
    I386(0x014c),
    R3000(0x0162),
    R4000(0x0166),
    R10000(0x0168),
    WCEMIPSV2(0x0169),
    ALPHA(0x0184),
    SH3(0x01a2),
    SH3DSP(0x01a3),
    SH3E(0x01a4),
    SH4(0x01a6),
    SH5(0x01a8),
    ARM(0x01c0),
    THUMB(0x01c2),
    ARMNT(0x01c4),
    AM33(0x01d3),
    POWERPC(0x01f0),
    POWERPCFP(0x01f1),
    IA64(0x0200),
    MIPS16(0x0266),
    ALPHA64(0x0284),
    MIPSFPU(0x0366),
    MIPSFPU16(0x0466),
    AXP64(ALPHA64.value),
    TRICORE(0x0520),
    CEF(0x0cef),
    EBC(0x0ebc),
    AMD64(0x8664),
    M32R(0x9041),
    CEE(0xc0ee);

    companion object {
        fun fromValue(value: Int) = values().first { it.value == value }
    }
}