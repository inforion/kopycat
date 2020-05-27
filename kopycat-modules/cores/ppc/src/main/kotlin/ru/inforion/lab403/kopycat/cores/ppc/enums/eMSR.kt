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



enum class eMSR(val bit: Int) {
    CM(31),         //Computation mode (0 - 32 bit, 1 - 64 bit)
    //30 - reserved
    //29 - implementation-dependent
    GS(28),         //Guest state
    //27 - implementation-dependent
    UCLE(26),       //User cache locking enable (0 - cache locking's are privileged, 1 - can be exec in user mode)
    SPV(25),        //SP/Embedded floating-point/Vector available
    //24 - reserved
    VSX(23),        //VSX available (1 - the thread can execute VSX instructions)
    //22-18 - reserved
    CE(17),         //Critical enable (1 - critical interrupts are enabled)
    //16 - reserved
    EE(15),         //External enable (1 - external interrupts are enabled)
    PR(14),         //Problem state (0 - the thread is in privileged state, 1 - in problem state)
    FP(13),         //Floating-point available (1 - the thread can execute floating-point instructions)
    ME(12),         //Machine check enable (1 - machine check interrupts are enabled)
    FE0(11),        //Floating-point exception mode 0
    //10 - implementation-dependent
    DE(9),         //Debug interrupt enable (1 - debug interrupts are enabled if DBCR0[IDM] == 1)
    FE1(8),         //Floating-point exception mode 1
    //7 - reserved
    //6 - reserved
    IS(5),          //Instruction address space
    DS(4),          //Data address space
    //3 - implementation-dependent
    PMM(2);         //Performance monitor mark (1 - enable statistics gathering on marked processes)
    //1 - reserved
    //0 - reserved
}