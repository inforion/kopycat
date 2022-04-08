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
@file:Suppress("FunctionName", "NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.modules.atom2758

import ru.inforion.lab403.common.extensions.ULONG_MAX
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.Bus
import ru.inforion.lab403.kopycat.cores.base.MasterPort
import ru.inforion.lab403.kopycat.cores.base.Register
import ru.inforion.lab403.kopycat.cores.base.SlavePort
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.BUS32
import ru.inforion.lab403.kopycat.modules.BUS40
import java.util.logging.Level
import java.util.logging.Level.*

const val MESSAGE_BUS_SIZE = BUS40
const val MESSAGE_PORT_SIZE = BUS32
const val MESSAGE_BUS_OFFSET_BITS = 32

const val MESSAGE_BUS_SERVICE_ADDR = 0xFFFF_FFFFuL  // Hope no other register will be here

const val MESSAGE_BUS_DEVICE_NOT_SUPPORTED = ULONG_MAX

const val MESSAGE_BUS_READ_OPERATION = 0xA0uL
const val MESSAGE_BUS_WRITE_OPERATION = 0xB0uL
const val MESSAGE_BUS_UNKNOWN_OPERATION = 0xC0uL

fun messageBusAddress(port: ULong, ext: ULong, offset: ULong) =
    (port shl MESSAGE_BUS_OFFSET_BITS) or ext or offset

fun <T : ModulePorts.APort> T.msg_connect(msg: Bus, port: Int = 0, ext: ULong = 0u, offset: ULong = 0u) =
    connect(msg, messageBusAddress(port.ulong_z, ext, offset))

fun MasterPort.requestOperationType(port: ULong, opcode: Int): ULong {
    val serviceAddress = messageBusAddress(port, 0u, MESSAGE_BUS_SERVICE_ADDR)
    return when {
        access(serviceAddress, opcode, 1) -> read(serviceAddress, opcode, 1)
        else -> MESSAGE_BUS_DEVICE_NOT_SUPPORTED
    }
}

inline fun Module.MESSAGE_BUS_SERVICE_REGISTER(
    port: SlavePort,
    noinline onRead: (ea: ULong, ss: Int, size: Int) -> ULong
) = object : Register(
    port,
    MESSAGE_BUS_SERVICE_ADDR,
    Datatype.BYTE,
    "SERVICE",
    level = CONFIG
) {
    override fun read(ea: ULong, ss: Int, size: Int) = onRead(ea, ss, size)

    override fun write(ea: ULong, ss: Int, size: Int, value: ULong): Unit = error("Write not supported")
}