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
package ru.inforion.lab403.kopycat.veos.api.abstracts

import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult


abstract class APIFunction constructor(name: String, address: Long? = null) : APIObject(name, address) {
    abstract val args: Array<ArgType> //TODO: default C types: char, int, short, long, etc...
    abstract fun exec(name: String, vararg argv: Long): APIResult

    open fun retval(value: Long) = APIResult.Value(value)

    open fun void() = APIResult.Void()

    fun redirect(address: Long) = APIResult.Redirect(address)

    fun terminate(status: Int) = APIResult.Terminate(status)

    fun threadexit() = APIResult.ThreadExited()
}