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
package ru.inforion.lab403.kopycat.cores.ppc.enums.systems.e500v2



enum class eOEA_e500v2(val id: Int) {
    /* [1] - These registers are defined by EIS
    *  [2] - e500v2 only
    *  [3] - These registers are e500-specific
    * */

    //Interrupt registers

    //Critical SRR 0/1
    CSRR0(58),
    CSRR1(59),

    //Machine check SRR 0/1 [1]
    MCSRR0(570),
    MCSRR1(571),

    //Exception syndrome register
    ESR(62),

    //Machine check syndrome register [1]
    MCSR(572),

    //MCAR - 573

    //Data exception address register
    DEAR(61),

    //Interrupt vector offset registers 0-15
    IVOR0(400),
    IVOR1(401),
    IVOR2(402),
    IVOR3(403),
    IVOR4(404),
    IVOR5(405),
    IVOR6(406),
    IVOR7(407),
    IVOR8(408),
    IVOR9(409),
    IVOR10(410),
    IVOR11(411),
    IVOR12(412),
    IVOR13(413),
    IVOR14(414),
    IVOR15(415),

    // L1 Cache (Read-Only)
    L1CFG0(515),
    L1CFG1(516),

    //Interrupt vector offset registers 32-35 [1]
    IVOR32(528),
    IVOR33(529),
    IVOR34(530),
    IVOR35(531),

    //Debug registers

    //Debug control registers 0-2
    DBCR0(308),
    DBCR1(309),
    DBCR2(310),

    //Debug status register
    DBSR(304),

    //Instruction address compare registers 1 and 2
    IAC1(312),
    IAC2(313),

    //Data address compare registers 1 and 2
    DAC1(316),
    DAC2(317),

    //MMU control and status (Read only)

    //MMU configuration [1]
    //MMUCFG(1015),

    //TLB configuration 0/1 [1]
    //TLB0CFG - 688
    //TLB1CFG - 689

    //L1 Cache (Read/Write)

    //L1 cache control/status 0/1 [1]
    L1CSR0(1010),
    L1CSR1(1011),

    // Configuration registers
    SVR(1023),
    PIR(286),
    // PVR - 287

    //TODO: Many skipped

    //Miscellaneous registers
    HID0(1008),
    HID1(1009),

    //Branch control and status register [3]
    BUCSR(1013);

}