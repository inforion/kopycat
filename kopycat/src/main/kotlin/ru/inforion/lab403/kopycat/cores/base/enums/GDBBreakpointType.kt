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
package ru.inforion.lab403.kopycat.cores.base.enums

import ru.inforion.lab403.kopycat.cores.base.common.Breakpoint

/** GDB breakpoint type */
enum class GDBBreakpointType(val code: Int) {
    /** Z0, --X */
    SOFTWARE(0),
    /** Z1, --X */
    HARDWARE(1),
    /** Z2, write watchpoint, -W- */
    WRITE(2),
    /** Z3, read watchpoint, R-- */
    READ(3),
    /** Z4, access watchpoint, RW- */
    ACCESS(4);

    inline val access get() = when (this) {
        SOFTWARE, HARDWARE -> Breakpoint.Access.EXEC
        WRITE -> Breakpoint.Access.WRITE
        READ -> Breakpoint.Access.READ
        ACCESS -> Breakpoint.Access.RW
    }
}
