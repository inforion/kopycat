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



enum class ExcCode(val id: Long) {
    INT(0),         // Interrupt
    MOD(1),         // TLB modification exception
    TLBL_INVALID(2),     // TLB exception (load or instruction fetch)
    TLBS_INVALID(3),     // TLB exception (store)
    TLBL_MISS(2),        // TLB exception (load or instruction fetch)
    TLBS_MISS(3),        // TLB exception (store)
    ADEL(4),        // Address error exception (load or instruction fetch)
    ADES(5),        // Address error exception (store)
    IBE(6),         // Bus error exception (instruction fetch)
    DBE(7),         // Bus error exception (data reference: load or store)
    SYS(8),         // Syscall exception
    BP(9),          // Breakpoint exception
    RI(10),         // Reserved instruction exception
    CPU(11),        // Coprocessor Unusable exception
    OV(12),         // Arithmetic Overflow exception
    TR(13),         // Trap exception
    FPE(15),        // Floating point exception
    C2E(18),        // Reserved for precise Coprocessor 2 exceptions
    MDMX(22),       // Reserved for MDMX Unusable Exception in MIPS64 implementations.
    WATCH(23),      // Reference to WatchHi/WatchLo address
    MCHECK(24),     // Machine check
    CACHEERR(30)    // Cache error
}