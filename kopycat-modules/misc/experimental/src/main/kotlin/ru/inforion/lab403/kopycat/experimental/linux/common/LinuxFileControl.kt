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


/**
 * Do not use it as a dataclass
 */
class LinuxFileControlBuilder {
    val flags = mutableSetOf<LinuxFileControl>()

    val RDONLY: Unit
        get() {
            flags.add(LinuxFileControl.RDONLY)
        }
    val WRONLY: Unit
        get() {
            flags.add(LinuxFileControl.WRONLY)
        }
    val CREAT: Unit
        get() {
            flags.add(LinuxFileControl.CREAT)
        }
    val TRUNC: Unit
        get() {
            flags.add(LinuxFileControl.TRUNC)
        }
    val DSYNC: Unit
        get() {
            flags.add(LinuxFileControl.DSYNC)
        }
    val DIRECT: Unit
        get() {
            flags.add(LinuxFileControl.DIRECT)
        }
    val LARGEFILE: Unit
        get() {
            flags.add(LinuxFileControl.LARGEFILE)
        }
    val DIRECTORY: Unit
        get() {
            flags.add(LinuxFileControl.DIRECTORY)
        }
    val NOFOLLOW: Unit
        get() {
            flags.add(LinuxFileControl.NOFOLLOW)
        }
    val CLOEXEC: Unit
        get() {
            flags.add(LinuxFileControl.CLOEXEC)
        }

    val result: Int get() = flags.map{it.code}.reduce{acc, fc -> acc or fc}
}

fun buildLinuxFileControl(block: LinuxFileControlBuilder.() -> Unit): Int {
    val builder = LinuxFileControlBuilder()
    block(builder)
    return builder.result
}

/**
 * [Sources on bootlin](https://elixir.bootlin.com/linux/v3.2.16/source/include/asm-generic/fcntl.h)
 */
enum class LinuxFileControl(val code: Int, val description: String) {
    RDONLY(0, "RdOnly"),
    WRONLY(1, "WrOnly"),
    CREAT(0x40, "Creat"),
    TRUNC(0x200, "Trunc"),
    DSYNC(0x1000, "DSync"),
    DIRECT(0x4000, "Direct"),
    LARGEFILE(0x8000, "LargeFile"),
    DIRECTORY(0x1_0000, "Directory"),
    NOFOLLOW(0x2_0000, "NoFollow"),
    CLOEXEC(0x8_0000, "CloExec");
}
