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
package ru.inforion.lab403.kopycat.cores.ppc.instructions

import ru.inforion.lab403.common.extensions.*


/**
 * Sign extension to 32-bit integer
 * @param sbit sign bit
 * @return extended integer
 * */
infix fun Long.ssext(sbit: Int) = signext(this, sbit + 1).toLong()
infix fun Long.usext(sbit: Int) = signext(this, sbit + 1).toULong()

//Now it works and works faster
infix fun Long.rotl32(amount: Int): Long = ((this shl amount) or (this shr (32 - amount))).mask(31)

fun Long.replace(indx: Int, value: Long): Long = (this and (1L.shl(indx).inv()) or (value shl indx))
fun Long.replace(indx: Int, value: Boolean): Long = this.replace(indx, value.toLong())
fun Long.replace(indx: IntRange, value: Long): Long = (this and (bitMask(indx).inv()) or (value shl indx.last))


