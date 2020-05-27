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



//User instruction set architecture
enum class eUISA(val id: Int, val regName : String) {

    //General-purpose registers
    GPR0(0, "R0"),
    GPR1(1, "R1"),
    GPR2(2, "R2"),
    GPR3(3, "R3"),
    GPR4(4, "R4"),
    GPR5(5, "R5"),
    GPR6(6, "R6"),
    GPR7(7, "R7"),
    GPR8(8, "R8"),
    GPR9(9, "R9"),
    GPR10(10, "R10"),
    GPR11(11, "R11"),
    GPR12(12, "R12"),
    GPR13(13, "R13"),
    GPR14(14, "R14"),
    GPR15(15, "R15"),
    GPR16(16, "R16"),
    GPR17(17, "R17"),
    GPR18(18, "R18"),
    GPR19(19, "R19"),
    GPR20(20, "R20"),
    GPR21(21, "R21"),
    GPR22(22, "R22"),
    GPR23(23, "R23"),
    GPR24(24, "R24"),
    GPR25(25, "R25"),
    GPR26(26, "R26"),
    GPR27(27, "R27"),
    GPR28(28, "R28"),
    GPR29(29, "R29"),
    GPR30(30, "R30"),
    GPR31(31, "R31"),

    //TODO: move to separate enum
    //Floating-point registers
    FPR0(32, "FPR0"),
    FPR1(33, "FPR1"),
    FPR2(34, "FPR2"),
    FPR3(35, "FPR3"),
    FPR4(36, "FPR4"),
    FPR5(37, "FPR5"),
    FPR6(38, "FPR6"),
    FPR7(39, "FPR7"),
    FPR8(40, "FPR8"),
    FPR9(41, "FPR9"),
    FPR10(42, "FPR10"),
    FPR11(43, "FPR11"),
    FPR12(44, "FPR12"),
    FPR13(45, "FPR13"),
    FPR14(46, "FPR14"),
    FPR15(47, "FPR15"),
    FPR16(48, "FPR16"),
    FPR17(49, "FPR17"),
    FPR18(50, "FPR18"),
    FPR19(51, "FPR19"),
    FPR20(52, "FPR20"),
    FPR21(53, "FPR21"),
    FPR22(54, "FPR22"),
    FPR23(55, "FPR23"),
    FPR24(56, "FPR24"),
    FPR25(57, "FPR25"),
    FPR26(58, "FPR26"),
    FPR27(59, "FPR27"),
    FPR28(60, "FPR28"),
    FPR29(61, "FPR29"),
    FPR30(62, "FPR30"),
    FPR31(63, "FPR31"),

    //Condition register
    CR(64, "CR"),

    //TODO: move to separate enum
    //Floating-point status and control register
    FPSCR(65, "FPSCR"),

    //Fixed-point exception register
    XER(66, "XER"),

    //Link register
    LR(67, "LR"),

    //Count register
    CTR(68, "CTR"),

    //Program counter
    //Not a real register in PPC system
    PC(69, "PC"),

    //Memory synchronisation subsystem
    RESERVE(70, "RESERVE"),
    RESERVE_ADDR(71, "RESERVE");


    companion object {
        fun from(id: Int): eUISA = eUISA.values().first { it.id == id }
    }
}