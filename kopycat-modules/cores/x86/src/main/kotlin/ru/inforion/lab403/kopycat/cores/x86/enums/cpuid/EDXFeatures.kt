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

enum class EDXFeatures(val id: Int) {
    // 0
    fpu(0),
    vme(1),
    de(2),
    pse(3),
    tsc(4),
    msr(5),
    pae(6),
    mce(7),
    cx8(8),
    apic(9),

    sep(11),
    mtrr(12),
    pge(13),
    mca(14),
    cmov(15),
    pat(16),
    pse36(17),
    psn(18),
    clflush(19),

    dts(21),
    acpi(22),
    mmx(23),
    fxsr(24),
    sse(25),
    sse2(26),
    ss(27),
    ht(28),
    tm(29),
    ia64(30),
    pbe(31),

    // 0x8000_0001
    syscall(11),

    mp(19),
    nx(20),

    mmxent(22),

    fxsr_opt(25),
    gbpages(26),
    rdtscp(27),

    lm(29),
    `3dnowext`(30),
    `3dnow`(31)
}