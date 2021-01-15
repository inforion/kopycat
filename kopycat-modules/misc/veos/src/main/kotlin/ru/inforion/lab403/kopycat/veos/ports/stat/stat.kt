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
package ru.inforion.lab403.kopycat.veos.ports.stat

data class stat(
        val st_dev: Long = 0,
        val st_ino: Long = 0,
        val st_mode: Long = 0,
        val st_nlink: Long = 0,
        val st_uid: Long = 0,
        val st_gid: Long = 0,
        val st_rdev: Long = 0,
        val st_size: Long = 0,
        val st_blksize: Long = 0,
        val st_blocks: Long = 0,
        val st_atime: Long = 0,
        val st_mtime: Long = 0,
        val st_ctime: Long = 0)