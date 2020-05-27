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



enum class eCR(val bit: Int) {
    //CR0
    CR0_LT(31), //Negative
    CR0_GT(30), //Positive
    CR0_EQ(29), //Zero
    CR0_SO(28), //Summary overflow

    //CR1
    CR1_FX(27), //Floating-point exception
    CR1_FEX(26), //Floating-point enabled exception
    CR1_VX(25), //Floating-point invalid exception
    CR1_OX(24); //Floating-point overflow exception

    companion object {
        fun LTbit(ind: Int): Int = CR0_LT.bit - 4 * ind
        fun GTbit(ind: Int): Int = CR0_GT.bit - 4 * ind
        fun EQbit(ind: Int): Int = CR0_EQ.bit - 4 * ind
        fun SObit(ind: Int): Int = CR0_SO.bit - 4 * ind
        fun msb(ind: Int): Int = LTbit(ind)
        fun lsb(ind: Int): Int = SObit(ind)
    }
}