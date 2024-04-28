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
package ru.inforion.lab403.kopycat.experimental.linux.common.file

class x86LinuxFileControl : ILinuxFileControl {
    override val RDONLY: Int = 0
    override val WRONLY: Int = 1
    override val CREAT: Int = 0x40
    override val TRUNC: Int = 0x200
    override val DSYNC: Int = 0x1000
    override val DIRECT: Int = 0x4000
    override val LARGEFILE: Int = 0x8000
    override val DIRECTORY: Int = 0x1_0000
    override val NOFOLLOW: Int = 0x2_0000
    override val CLOEXEC: Int = 0x8_0000
}