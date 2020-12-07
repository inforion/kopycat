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
package ru.inforion.lab403.elfloader.processors.arm.enums


 
enum class ArmFlags(val id: Int) {
    EF_ARM_ABIMASK(0xFF000000.toInt()),        //ABI version
    EF_ARM_BE8(0x00800000),                 //BE-8 code for armv6
    EF_ARM_GCCMASK(0x00400FFF),             //Legacy code of GCC
    EF_ARM_ABI_FLOAT_HARD(0x00000400),      //Hard float
    EF_ARM_ABI_FLOAT_SOFT(0x00000200);      //Soft float, emulation
}