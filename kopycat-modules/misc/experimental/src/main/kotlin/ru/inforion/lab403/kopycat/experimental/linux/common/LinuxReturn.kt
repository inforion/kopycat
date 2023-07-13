/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.kopycat.experimental.linux.common

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.experimental.linux.exception.LinuxErrorException
import ru.inforion.lab403.kopycat.experimental.linux.exception.LinuxKnownErrorException

sealed class LinuxReturn(val rawValue: ULong) {

    class Success(value: ULong) : LinuxReturn(value)
    class UnknownError(value: ULong) : LinuxReturn(value)
    class KnownError(val status: LinuxStatus) : LinuxReturn(status.code.ulong_z)

    fun getOrThrow() = when (this) {
        is Success -> rawValue
        is UnknownError -> throw LinuxErrorException(rawValue)
        is KnownError -> throw LinuxKnownErrorException(status)
    }

    companion object {
        fun fromReturnCode(code: ULong): LinuxReturn = if (code.long in (-4096L..-1L)) {
            val errorCode = -code.long
            LinuxStatus.fromOrNull(errorCode.int)
                ?.let { KnownError(it) }
                ?: UnknownError(errorCode.ulong)
        } else {

            Success(code)
        }
    }
}

fun ULong.toLinuxReturn() = LinuxReturn.fromReturnCode(this)

fun ULong.toLinuxSuccess() = LinuxReturn.fromReturnCode(this).getOrThrow()
