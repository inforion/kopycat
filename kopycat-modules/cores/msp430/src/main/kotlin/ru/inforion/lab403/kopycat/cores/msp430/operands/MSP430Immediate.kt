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
package ru.inforion.lab403.kopycat.cores.msp430.operands

import ru.inforion.lab403.common.extensions.WRONGI
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core



class MSP430Immediate(dtyp: Datatype, value: Long, signed: Boolean) : Immediate<MSP430Core>(value, signed, dtyp, WRONGI)
fun zero(dtyp: Datatype) = MSP430Immediate(dtyp, 0, true)
fun one(dtyp: Datatype) = MSP430Immediate(dtyp, 1, true)
fun two(dtyp: Datatype) = MSP430Immediate(dtyp, 2, true)
fun four(dtyp: Datatype) = MSP430Immediate(dtyp, 4, true)
fun eight(dtyp: Datatype) = MSP430Immediate(dtyp, 8, true)
fun negOne(dtyp: Datatype) = MSP430Immediate(dtyp, -1, true)
