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
package ru.inforion.lab403.kopycat.cores.x86.enums.cpuid

enum class BrandIndex(val id: UInt) {
    NotSupport(0x00u),
    Celeron_A(0x01u),
    PentiumIII_A(0x02u),
    PentiumIIIXenon(0x03u),
    PentiumIII_B(0x04u),

    MobilePentiumIIIM(0x06u),
    MobileCeleron_A(0x07u),
    Pentium4_A(0x08u),
    Pentium4_B(0x09u),
    Celeron_B(0x0Au),
    Xenon(0x0Bu),
    XenonMP(0x0Cu),

    MobilePentium4M(0x0Eu),
    MobileCeleron_B(0x0Fu),

    MobileGenuine_A(0x11u),
    CeleronM(0x12u),
    Celeron_C(0x13u),
    MobileCeleron_C(0x14u),
    MobileGenuine_B(0x15u),
    PentiumM(0x16u),
    MobileCeleron_D(0x17u)
}