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

enum class ECXFeatures(val id: Int) {
    pni(0), // SSE-3, Prescott New Instructions
    pclmulqdq(1),
    dtes64(2),
    monitor(3),
    ds_cpl(4),
    vmx(5),
    smx(6),
    est(7),
    tm2(8),
    ssse3(9),
    cnxtid(10),
    sdbg(11),
    fma(12),
    cx16(13),
    xtpr(14),
    pdcm(15),

    pcid(17),
    dca(18),
    sse41(19),
    sse42(20),
    x2apic(21),
    movbe(22),
    popcnt(23),
    tscdeadline(24),
    aes(25),
    xsave(26),
    osxsave(27),
    avx(28),
    f16c(29),
    rdrnd(30),
    hypervisor(31),

    // 0x8000_0001
    lahf_lm(0),
    cmp_legacy(1),
    svm(2),
    extapic(3),
    cr8_legacy(4),
    abm(5),
    sse4a(6),
    masalignsse(7),
    `3dnowprefetch`(8),
    osvw(9),
    ibs(10),
    xop(11),
    skinit(12),
    wdt(13),

    lwp(15),
    fma4(16),

    nodeid_msr(19),

    tbm(21),
    topoext(22),
    perfctr_core(23)
}
