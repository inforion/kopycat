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
package ru.inforion.lab403.kopycat.cores.ppc.enums



//Operating environment architecture
enum class eOEA(val id: Int, val regName : String) {
    //<Configuration registers>
    //Machine state register
    MSR(0, "MSR"),

    //Processor version register
    PVR(1, "PVR"),

    //TODO: move to SPRs?
    //<Memory management registers>
    //Instruction BAT registers
    /*IBAT0U(2, "IBAT0U"),
    IBAT0L(3, "IBAT0L"),
    IBAT1U(4, "IBAT1U"),
    IBAT1L(5, "IBAT1L"),
    IBAT2U(6, "IBAT2U"),
    IBAT2L(7, "IBAT2L"),
    IBAT3U(8, "IBAT3U"),
    IBAT3L(9, "IBAT3L"),

    //Data BAT registers
    DBAT0U(10, "DBAT0U"),
    DBAT0L(11, "DBAT0L"),
    DBAT1U(12, "DBAT1U"),
    DBAT1L(13, "DBAT1L"),
    DBAT2U(14, "DBAT2U"),
    DBAT2L(15, "DBAT2L"),
    DBAT3U(16, "DBAT3U"),
    DBAT3L(17, "DBAT3L"),*/

    //SDR1
    SDR1(2, "SDR1"),

    //Address space register
    ASR(3, "ASR"),

    //Segment registers
    SR0(4, "SR0"),
    SR1(5, "SR1"),
    SR2(6, "SR2"),
    SR3(7, "SR3"),
    SR4(8, "SR4"),
    SR5(9, "SR5"),
    SR6(10, "SR6"),
    SR7(11, "SR7"),
    SR8(12, "SR8"),
    SR9(13, "SR9"),
    SR10(14, "SR10"),
    SR11(15, "SR11"),
    SR12(16, "SR12"),
    SR13(17, "SR13"),
    SR14(18, "SR14"),
    SR15(19, "SR15"),


    //<Exception handling registers>
    //Data address register
    DAR(20, "DAR"),

    //DSISR
    DSISR(21, "DSISR"),

    //SPRGs
    SPRG0(22, "SPRG0"),
    SPRG1(23, "SPRG1"),
    SPRG2(24, "SPRG2"),
    SPRG3(25, "SPRG3"),

    //Save and restore registers
    SRR0(26, "SRR0"),
    SRR1(27, "SRR1"),

    //Floating-point exception cause register (optional)
    FPECR(28, "FPECR"),


    //<Miscellaneous registers>
    //Time base facility (for writing)
    TBL(29, "TBL"),
    TBU(30, "TBU"),

    //Data address breakpoint register (optional)
    DABR(31, "DABR"),

    //TimeBase
    DEC(32, "DEC"),

    //External access register (optional)
    EAR(33, "EAR"),

    //Processor identification register (optional)
    PIR(34, "PIR");

    companion object {
        fun from(id: Int): eVEA = eVEA.values().first { it.id == id }
    }
}