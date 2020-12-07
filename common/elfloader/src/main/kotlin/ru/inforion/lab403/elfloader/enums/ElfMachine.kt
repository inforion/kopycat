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
package ru.inforion.lab403.elfloader.enums


 
enum class ElfMachine(val id: Short) {
    EM_NONE(0),     //No machine
    EM_M32(1),      //AT&T WE 32100
    EM_SPARC(2),    //SPARC
    EM_386(3),      //Intel 80386
    EM_68K(4),      //Motorola 68000
    EM_88K(5),      //Motorola 68000
    EM_IAMCU(6),    //Intel MCU
    EM_860(7),      //Intel 80860
    EM_MIPS(8),     //MIPS I Architecture

    EM_ARM(40);     //ARM
}