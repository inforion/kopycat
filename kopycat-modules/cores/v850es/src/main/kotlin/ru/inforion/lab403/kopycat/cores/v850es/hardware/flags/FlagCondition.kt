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
package ru.inforion.lab403.kopycat.cores.v850es.hardware.flags

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

@Suppress("NOTHING_TO_INLINE")


object FlagCondition {
    inline fun CheckCondition(core: v850ESCore, cc: Long): Boolean = when (cc) {
        0x0L -> core.cpu.flags.ov
        0x1L -> core.cpu.flags.cy
        0x2L -> core.cpu.flags.z
        0x3L -> core.cpu.flags.cy or core.cpu.flags.z
        0x4L -> core.cpu.flags.s
        0x5L -> true
        0x6L -> core.cpu.flags.s xor core.cpu.flags.ov
        0x7L -> (core.cpu.flags.s xor core.cpu.flags.ov) or core.cpu.flags.z
        0x8L -> !core.cpu.flags.ov
        0x9L -> !core.cpu.flags.cy
        0xAL -> !core.cpu.flags.z
        0xBL -> !(core.cpu.flags.cy or core.cpu.flags.z)
        0xCL -> !core.cpu.flags.s
        0xDL -> core.cpu.flags.sat
        0xEL -> !(core.cpu.flags.s xor core.cpu.flags.ov)
        0xFL -> !((core.cpu.flags.s xor core.cpu.flags.ov) or core.cpu.flags.z)
        else -> throw GeneralException("Incorrect condition code in CheckCondition")
    }
}