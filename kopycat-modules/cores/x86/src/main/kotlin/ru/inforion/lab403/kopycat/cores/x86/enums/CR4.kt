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
package ru.inforion.lab403.kopycat.cores.x86.enums


enum class CR4(val bit: Int) {
    VME(0),
    PVI(1),
    TSD(2),
    DE(3),
    PSE(4),
    PAE(5),
    MCE(6),
    PGE(7),
    PCE(8),
    OSFXSR(9),
    OSXMMEXCPT(10),
    VMXE(13),
    SMXE(14),
    FSGSBASE(16),
    PCIDE(17),
    OSXSAVE(18),
    SMEP(20),
    SMAP(21),
    PKE(22);
}