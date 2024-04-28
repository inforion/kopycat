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
package ru.inforion.lab403.kopycat.experimental.linux.api.raw

import ru.inforion.lab403.kopycat.auxiliary.capturable.CapturableNoBody
import ru.inforion.lab403.kopycat.experimental.linux.api.interfaces.*
import ru.inforion.lab403.kopycat.interfaces.outq
import ru.inforion.lab403.kopycat.runtime.funcall.*
import ru.inforion.lab403.kopycat.runtime.abi.IAbi

class DefaultLinuxVfsRWApi(
    val abi: IAbi,
    override val PTR_VFS_READ: ULong,
    override val PTR_VFS_WRITE: ULong
) : LinuxVfsRWCapturableApi {
    override fun vfsReadCapturable(
        filePointer: ULong,
        size: ULong,
        fileIterator: ULong
    ) = object : VfsReadCapturable {
        lateinit var argsCapturable: CapturableNoBody

        override lateinit var alloca: StackAllocation
        override lateinit var pointer: StackAllocation

        override fun initialize() {
            alloca = abi.allocOnStack(size)
            pointer = abi.allocOnStack(0x8u)
            abi.core.outq(pointer.address, fileIterator)

            argsCapturable = abi.argsCapturable(
                filePointer.toArgPointer(),
                alloca.toArgPointer(),
                FunArg.Number(size),
                pointer.toArgPointer()
            )
            return argsCapturable.initialize()
        }

        override fun destroy() {
            argsCapturable.destroy()

            abi.clearStackAllocation(pointer)
            abi.clearStackAllocation(alloca)
        }
    }

    override fun vfsWriteCapturable(
        filePointer: ULong,
        content: ByteArray,
        fileIterator: ULong
    ) = object : VfsWriteCapturable {
        lateinit var argsCapturable: CapturableNoBody

        override lateinit var pointer: StackAllocation

        override fun initialize() {
            pointer = abi.allocOnStack(0x8u)
            abi.core.outq(pointer.address, fileIterator)

            argsCapturable = abi.argsCapturable(
                filePointer.toArgPointer(),
                content.toArg(),
                FunArg.Number(content.size),
                pointer.toArgPointer()
            )
            return argsCapturable.initialize()
        }

        override fun destroy() {
            argsCapturable.destroy()

            abi.clearStackAllocation(pointer)
        }
    }
}