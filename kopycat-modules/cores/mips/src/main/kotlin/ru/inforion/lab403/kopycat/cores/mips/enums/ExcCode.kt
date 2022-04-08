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
package ru.inforion.lab403.kopycat.cores.mips.enums



enum class ExcCode(val id: ULong) {
    INT(0u),         // Interrupt
    MOD(1u),         // TLB modification exception
    TLBL_INVALID(2u),     // TLB exception (load or instruction fetch)
    TLBS_INVALID(3u),     // TLB exception (store)
    TLBL_MISS(2u),        // TLB exception (load or instruction fetch)
    TLBS_MISS(3u),        // TLB exception (store)
    ADEL(4u),        // Address error exception (load or instruction fetch)
    ADES(5u),        // Address error exception (store)
    IBE(6u),         // Bus error exception (instruction fetch)
    DBE(7u),         // Bus error exception (data reference: load or store)
    SYS(8u),         // Syscall exception
    BP(9u),          // Breakpoint exception
    RI(10u),         // Reserved instruction exception
    CPU(11u),        // Coprocessor Unusable exception
    OV(12u),         // Arithmetic Overflow exception
    TR(13u),         // Trap exception
    FPE(15u),        // Floating point exception
    C2E(18u),        // Reserved for precise Coprocessor 2 exceptions
    MDMX(22u),       // Reserved for MDMX Unusable Exception in MIPS64 implementations.
    WATCH(23u),      // Reference to WatchHi/WatchLo address
    MCHECK(24u),     // Machine check
    CACHEERR(30u)    // Cache error
}