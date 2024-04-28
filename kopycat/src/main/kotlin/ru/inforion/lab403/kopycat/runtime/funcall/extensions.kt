/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
package ru.inforion.lab403.kopycat.runtime.funcall

import ru.inforion.lab403.kopycat.auxiliary.capturable.CapturableNoBody
import ru.inforion.lab403.kopycat.runtime.abi.IAbi

fun StackAllocation.toArgPointer() = FunArg.Pointer(this.address)
fun ULong.toArgNumber() = FunArg.Number(this)
fun ULong.toArgPointer() = FunArg.Pointer(this)

fun ByteArray.toArg() = FunArg.ByteArray(this)
fun String.toArg() = FunArg.String(this)

fun IAbi.argsCapturable(vararg args: FunArg): CapturableNoBody = object : CapturableNoBody {
    lateinit var allocas: List<StackAllocation>

    override fun initialize() {
        allocas = allocAndPutArgs(args.toList())
    }

    override fun destroy() {
        allocas.reversed().forEach {
            clearStackAllocation(it)
        }
    }
}
