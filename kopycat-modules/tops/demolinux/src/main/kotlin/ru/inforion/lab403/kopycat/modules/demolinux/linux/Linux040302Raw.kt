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
package ru.inforion.lab403.kopycat.modules.demolinux.linux

import ru.inforion.lab403.kopycat.runtime.abi.IAbi
import ru.inforion.lab403.kopycat.experimental.linux.api.interfaces.*
import ru.inforion.lab403.kopycat.experimental.linux.api.raw.*

private object RawPointers {
    val PRINTK: ULong = 0xFFFFFFFF8108A6CDuL
    val FILP_OPEN: ULong = 0xFFFFFFFF810BF8EBuL
    val FILP_CLOSE: ULong = 0xFFFFFFFF810BE9A4uL
    val VFS_READ: ULong = 0xFFFFFFFF810C0341uL
    val VFS_WRITE: ULong = 0xFFFFFFFF810C043DuL
    val VFS_FSYNC: ULong = 0xFFFFFFFF810E1BE6uL
    val SYS_CHMOD: ULong = 0xFFFFFFFF810BF591uL
    val SYS_UNLINK: ULong = 0xFFFFFFFF810CC63CuL
    val SYS_MKDIR: ULong = 0xFFFFFFFF810CC5EBuL
}


class Linux040302Raw(private val abi: IAbi) :
    LinuxFilpCapturableApi by
    DefaultLinuxFilpApi(abi, RawPointers.FILP_OPEN, RawPointers.FILP_CLOSE),

    LinuxVfsRWCapturableApi by
    DefaultLinuxVfsRWApi(abi, RawPointers.VFS_READ, RawPointers.VFS_WRITE),

    LinuxPrintkCapturableApi by
    DefaultLinuxPrintkApi(abi, RawPointers.PRINTK),

    LinuxSysUnlinkCapturableApi by
    DefaultLinuxSysUnlinkApi(abi, RawPointers.SYS_UNLINK),

    LinuxSysChmodCapturableApi by
    DefaultLinuxSysChmodApi(abi, RawPointers.SYS_CHMOD),

    LinuxVfsSyncCapturableApi by
    DefaultLinuxVfsSyncApi(abi, RawPointers.VFS_FSYNC),

    LinuxSysMkdirCapturableApi by
    DefaultLinuxSysMkdirApi(abi, RawPointers.SYS_MKDIR)
