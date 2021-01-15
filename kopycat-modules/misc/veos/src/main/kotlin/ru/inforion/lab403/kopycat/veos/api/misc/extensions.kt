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
package ru.inforion.lab403.kopycat.veos.api.misc

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.veos.exceptions.BadAddressException
import ru.inforion.lab403.kopycat.veos.exceptions.CantDecodeVeosError
import ru.inforion.lab403.kopycat.veos.exceptions.InvalidArgument
import ru.inforion.lab403.kopycat.veos.exceptions.io.*
import ru.inforion.lab403.kopycat.veos.ports.posix.PosixError

// TODO: sort by posix error
fun Exception.toStdCErrno(where: Long) = when (this) {
    // If we got memory access error, then throw it because it is not usual for normal programs
    is MemoryAccessError -> throw this // PosixError.EFAULT
    is BadAddressException -> PosixError.EFAULT
    is IONotAppropriateDevice -> PosixError.ENOTTY
    is IONotConnected -> PosixError.ENOTCONN
    is IOConnectionAborted -> PosixError.ECONNABORTED
    is IONotReadyError -> PosixError.EAGAIN
    is IONotFoundError -> PosixError.EBADF
    is IONotSocket -> PosixError.ENOTSOCK
    is IOAddressInUse -> PosixError.EADDRINUSE
    is IOOperationNotSupported -> PosixError.EOPNOTSUPP
    is InvalidArgument -> PosixError.EINVAL
    is IONoSuchFileOrDirectory -> PosixError.ENOENT
    is IOFileExists -> PosixError.EEXIST
    else -> throw CantDecodeVeosError("[0x${where.hex8}] Unknown exception = $this")
}