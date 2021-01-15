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
package ru.inforion.lab403.kopycat.veos.api.cherubim

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult
import java.util.concurrent.LinkedBlockingQueue

/**
 * Two threads divides single execution time
 */
class Cherubim(val os: VEOS<*>, vararg val arguments: Long) {
    val toInterrupted = LinkedBlockingQueue<Long>()
    val fromInterrupted = LinkedBlockingQueue<APIResult>()

    fun interrupt(address: Long, vararg args: Long): Long {
        os.abi.push(os.abi.programCounterValue)
        os.abi.push(os.abi.returnAddressValue)
        arguments.forEach { os.abi.push(it) }
        os.abi.push(arguments.size.asULong)

        val stackPointer = os.abi.stackPointerValue

        os.abi.setArgs(args.toTypedArray(), true)

        val stackDifference = os.abi.stackPointerValue - stackPointer
        check(stackDifference.asInt % os.sys.sizeOf.int == 0) { "Not word-aligned stack" }
        val stackArgCount = stackDifference.asInt / os.sys.sizeOf.int

        os.abi.returnAddressValue = os.sys.restoratorAddress[stackArgCount]

        fromInterrupted.add(APIResult.Redirect(address))
        return toInterrupted.take()
    }
}
